package com.hospitality.mis.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospitality.mis.auth.adapter.RefreshTokenRepository;
import com.hospitality.mis.auth.api.AuthDtos;
import com.hospitality.mis.auth.application.JwtTokenService;
import com.hospitality.mis.governance.adapter.ApprovalRepository;
import com.hospitality.mis.governance.domain.ApprovalRequest;
import com.hospitality.mis.identity.adapter.EmployeeRepository;
import com.hospitality.mis.identity.domain.Employee;
import com.hospitality.mis.identity.domain.EmployeeRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.ORIGIN;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:securitytest;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class SecurityIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired EmployeeRepository employees;
    @Autowired ApprovalRepository approvals;
    @Autowired RefreshTokenRepository refreshTokens;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired UserDetailsService userDetailsService;
    @Autowired JwtEncoder jwtEncoder;
    @Autowired ObjectMapper objectMapper;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void seedUsers() {
        refreshTokens.deleteAll();
        approvals.deleteAll();
        employees.deleteAll();
        jdbc.update("delete from audit_logs");
        employees.save(employee("manager", "manager-password", EmployeeRole.MANAGER, "0900000001"));
        employees.save(employee("frontdesk", "frontdesk-password", EmployeeRole.FRONT_DESK, "0900000002"));
        employees.save(employee("staff", "staff-password", EmployeeRole.STAFF, "0900000003"));
        employees.save(employee("kitchen", "kitchen-password", EmployeeRole.KITCHEN, "0900000004"));
        employees.flush();
    }

    @Test
    void unauthenticatedApiRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/services"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void healthAndOpenApiRemainPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    void loginReturnsBearerTokensAndBasicIsRejected() throws Exception {
        JsonNode tokens = login("manager", "manager-password");
        assertThat(tokens.get("tokenType").asText()).isEqualTo("Bearer");
        assertThat(tokens.get("expiresIn").asLong()).isEqualTo(900L);
        assertThat(tokens.get("refreshExpiresIn").asLong()).isEqualTo(7L * 24 * 60 * 60);
        assertThat(tokens.get("accessToken").asText()).isNotBlank();
        assertThat(tokens.get("refreshToken").asText()).isNotBlank();

        mockMvc.perform(get("/api/services")
                        .header(AUTHORIZATION, bearer(tokens.get("accessToken").asText())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/services")
                        .header(AUTHORIZATION, basic("manager", "manager-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshRotatesAndReuseRevokesTheFamily() throws Exception {
        JsonNode first = login("manager", "manager-password");
        assertThat(refreshTokens.findAll()).anySatisfy(token -> {
            assertThat(token.getTokenHash()).isEqualTo(JwtTokenService.hash(first.get("refreshToken").asText()));
            assertThat(token.getTokenHash()).isNotEqualTo(first.get("refreshToken").asText());
        });
        JsonNode rotated = refresh(first.get("refreshToken").asText());

        assertThat(rotated.get("refreshToken").asText())
                .isNotEqualTo(first.get("refreshToken").asText());
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(APPLICATION_JSON)
                        .content(json(new AuthDtos.RefreshRequest(first.get("refreshToken").asText()))))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(APPLICATION_JSON)
                        .content(json(new AuthDtos.RefreshRequest(rotated.get("refreshToken").asText()))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRevokesRefreshToken() throws Exception {
        JsonNode tokens = login("manager", "manager-password");
        mockMvc.perform(post("/api/auth/logout")
                        .header(AUTHORIZATION, bearer(tokens.get("accessToken").asText()))
                        .contentType(APPLICATION_JSON)
                        .content(json(new AuthDtos.LogoutRequest(tokens.get("refreshToken").asText()))))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(APPLICATION_JSON)
                        .content(json(new AuthDtos.RefreshRequest(tokens.get("refreshToken").asText()))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidSignatureAndExpiredTokensAreRejected() throws Exception {
        JsonNode tokens = login("manager", "manager-password");
        String access = tokens.get("accessToken").asText();
        String[] parts = access.split("\\.");
        String invalidSignature = parts[0] + "." + parts[1] + "." + Base64.getUrlEncoder()
                .withoutPadding().encodeToString("invalid-signature".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(get("/api/services").header(AUTHORIZATION, bearer(invalidSignature)))
                .andExpect(status().isUnauthorized());

        String expired = jwtEncoder.encode(JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).type("JWT").keyId("hotel-mis").build(),
                        JwtClaimsSet.builder()
                                .subject("manager")
                                .issuedAt(Instant.now().minusSeconds(120))
                                .expiresAt(Instant.now().minusSeconds(60))
                                .claim("token_type", "access")
                                .build()))
                .getTokenValue();
        mockMvc.perform(get("/api/services").header(AUTHORIZATION, bearer(expired)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPlaintextAndMalformedCostCredentialsAreRejectedWith401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(json(new AuthDtos.LoginRequest("manager", "wrong-password"))))
                .andExpect(status().isUnauthorized());

        Employee plaintext = employee("plaintext", "plaintext-password", EmployeeRole.FRONT_DESK, "0900000005");
        plaintext.setPassword("plaintext-password");
        employees.saveAndFlush(plaintext);
        Employee malformedCost = employee("badcost", "unused", EmployeeRole.FRONT_DESK, "0900000006");
        malformedCost.setPassword("$2a$03$" + "A".repeat(53));
        employees.saveAndFlush(malformedCost);

        for (String employeeId : List.of("plaintext", "badcost")) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(json(new AuthDtos.LoginRequest(employeeId, "plaintext-password"))))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void failedLoginsAreAuditedAndLockTheAccountAtTheThreshold() throws Exception {
        for (int attempt = 0; attempt < 5; attempt++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(json(new AuthDtos.LoginRequest("staff", "wrong-password"))))
                    .andExpect(status().isUnauthorized());
        }

        Employee locked = employees.findById("staff").orElseThrow();
        assertThat(locked.getFailedLoginAttempts()).isEqualTo(5);
        assertThat(locked.isAccountNonLocked()).isFalse();
        assertThat(locked.getLastFailedLoginAt()).isNotNull();
        assertThat(jdbc.queryForObject(
                "select count(*) from audit_logs where actor = 'staff' and action = 'LOGIN_FAILED'",
                Integer.class)).isEqualTo(5);
        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(json(new AuthDtos.LoginRequest("staff", "staff-password"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void disabledAccountCannotAuthenticate() throws Exception {
        Employee disabled = employees.findById("staff").orElseThrow();
        disabled.setEnabled(false);
        employees.saveAndFlush(disabled);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(json(new AuthDtos.LoginRequest("staff", "staff-password"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void roleMappingIsExhaustiveAndUserDetailsHasRealAuthority() {
        for (EmployeeRole role : EmployeeRole.values()) assertEquals(role.name(), EmployeeUserDetailsService.roleFor(role));

        UserDetails user = userDetailsService.loadUserByUsername("manager");
        assertTrue(user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")));
        assertTrue(user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("PERMISSION_EMPLOYEE_PROVISION")));
        assertTrue(user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("PERMISSION_APPROVAL_APPROVE")));
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isEnabled());
    }

    @Test
    void onlyManagersCanApproveAndRequesterCannotSelfApprove() throws Exception {
        ApprovalRequest pending = approvals.saveAndFlush(
                new ApprovalRequest("frontdesk", "PRICE_OVERRIDE", "price-1", "manager review"));

        mockMvc.perform(post("/api/governance/approvals/{id}/approve", pending.getId())
                        .header(AUTHORIZATION, bearer(login("frontdesk", "frontdesk-password")
                                .get("accessToken").asText())))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/governance/approvals/{id}/approve", pending.getId())
                        .header(AUTHORIZATION, bearer(login("manager", "manager-password")
                                .get("accessToken").asText())))
                .andExpect(status().isOk());

        ApprovalRequest self = approvals.saveAndFlush(
                new ApprovalRequest("manager", "PRICE_OVERRIDE", "price-2", "cannot self approve"));
        mockMvc.perform(post("/api/governance/approvals/{id}/approve", self.getId())
                        .header(AUTHORIZATION, bearer(login("manager", "manager-password")
                                .get("accessToken").asText())))
                .andExpect(status().isForbidden());
    }

    @Test
    void auditActorComesFromSecurityContextNotHeader() throws Exception {
        mockMvc.perform(post("/api/governance/approvals")
                        .header(AUTHORIZATION, bearer(login("frontdesk", "frontdesk-password")
                                .get("accessToken").asText()))
                        .header("X-Actor-Id", "manager")
                        .contentType(APPLICATION_JSON)
                        .content("{\"action\":\"PRICE_OVERRIDE\",\"targetId\":\"price-2\",\"reason\":\"review\"}"))
                .andExpect(status().isCreated());

        String actor = jdbc.queryForObject(
                "select actor from audit_logs where action = 'APPROVAL_REQUESTED' order by id desc limit 1",
                String.class);
        assertEquals("frontdesk", actor);
        assertTrue(passwordEncoder.matches("manager-password",
                employees.findById("manager").orElseThrow().getPassword()));
    }

    @Test
    void mutatingEndpointRoleChecksReturn403() throws Exception {
        String staffToken = bearer(login("staff", "staff-password").get("accessToken").asText());
        mockMvc.perform(post("/api/auth/employees")
                        .header(AUTHORIZATION, staffToken)
                        .contentType(APPLICATION_JSON)
                        .content("{\"employeeId\":\"new\",\"fullName\":\"New\",\"password\":\"new-password\",\"role\":\"FRONT_DESK\",\"phone\":\"0900000007\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/services")
                        .header(AUTHORIZATION, staffToken)
                        .contentType(APPLICATION_JSON)
                        .content("{\"id\":\"S1\",\"name\":\"Water\",\"price\":1,\"openingStock\":0,\"safetyThreshold\":0}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerProvisioningAndPasswordResetAlwaysUseBcryptAndRevokeSessions() throws Exception {
        String managerToken = bearer(login("manager", "manager-password").get("accessToken").asText());
        mockMvc.perform(post("/api/auth/employees")
                        .header(AUTHORIZATION, managerToken)
                        .contentType(APPLICATION_JSON)
                        .content("{\"employeeId\":\"prov01\",\"fullName\":\"Provisioned\",\"password\":\"initial-password\",\"role\":\"FRONT_DESK\",\"phone\":\"0900000008\"}"))
                .andExpect(status().isCreated())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString()).doesNotContain("password"));

        String stored = employees.findById("prov01").orElseThrow().getPassword();
        assertTrue(EmployeeUserDetailsService.isBcryptHash(stored));
        assertTrue(passwordEncoder.matches("initial-password", stored));
        JsonNode provisionedTokens = login("prov01", "initial-password");

        mockMvc.perform(post(
                                "/api/auth/employees/{employeeId}/password", "prov01")
                        .header(AUTHORIZATION, managerToken)
                        .contentType(APPLICATION_JSON)
                        .content("{\"password\":\"reset-password\"}"))
                .andExpect(status().isNoContent());
        String resetStored = employees.findById("prov01").orElseThrow().getPassword();
        assertTrue(EmployeeUserDetailsService.isBcryptHash(resetStored));
        assertTrue(passwordEncoder.matches("reset-password", resetStored));
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(APPLICATION_JSON)
                        .content(json(new AuthDtos.RefreshRequest(provisionedTokens.get("refreshToken").asText()))))
                .andExpect(status().isUnauthorized());
        login("prov01", "reset-password");
    }

    @Test
    void corsAllowsConfiguredOriginAndRejectsUnconfiguredOrigin() throws Exception {
        mockMvc.perform(options("/api/services")
                        .header(ORIGIN, "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));

        mockMvc.perform(options("/api/services")
                        .header(ORIGIN, "https://evil.example")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }

    private JsonNode login(String employeeId, String password) throws Exception {
        return objectMapper.readTree(mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(json(new AuthDtos.LoginRequest(employeeId, password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
    }

    private JsonNode refresh(String refreshToken) throws Exception {
        return objectMapper.readTree(mockMvc.perform(post("/api/auth/refresh")
                        .contentType(APPLICATION_JSON)
                        .content(json(new AuthDtos.RefreshRequest(refreshToken))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private Employee employee(String id, String password, EmployeeRole role, String phone) {
        Employee employee = new Employee();
        employee.setEmployeeId(id);
        employee.setFullName(id);
        employee.setPassword(password.startsWith("plaintext-") ? password : passwordEncoder.encode(password));
        employee.setRole(role);
        employee.setPhone(phone);
        return employee;
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String basic(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}
