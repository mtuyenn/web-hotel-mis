package com.hospitality.mis.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospitality.mis.dao.auth.CustomerAccountRepository;
import com.hospitality.mis.dao.auth.RefreshTokenRepository;
import com.hospitality.mis.dao.identity.EmployeeRepository;
import com.hospitality.mis.dto.auth.AuthDtos;
import com.hospitality.mis.dto.auth.CustomerAccountDtos;
import com.hospitality.mis.entity.identity.Employee;
import com.hospitality.mis.entity.identity.EmployeeRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:customerauth;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class CustomerAuthenticationIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtDecoder jwtDecoder;
    @Autowired EmployeeRepository employees;
    @Autowired CustomerAccountRepository accounts;
    @Autowired RefreshTokenRepository refreshTokens;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        refreshTokens.deleteAll();
        accounts.deleteAll();
        employees.deleteAll();
        jdbc.update("delete from guests");
        jdbc.update("delete from audit_logs");
        Employee employee = new Employee();
        employee.setEmployeeId("employee");
        employee.setFullName("Employee");
        employee.setPassword(passwordEncoder.encode("employee-password"));
        employee.setRole(EmployeeRole.FRONT_DESK);
        employee.setPhone("0900000091");
        employees.saveAndFlush(employee);
    }

    @Test
    void customerRegistrationAndLoginUseCustomerPrincipalClaims() throws Exception {
        JsonNode registration = objectMapper.readTree(mockMvc.perform(post("/api/auth/customers/register")
                        .contentType(APPLICATION_JSON)
                        .content(json(new CustomerAccountDtos.RegisterRequest(
                                "0900000092", "customer-password", "Customer", "ID09000092"))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        String accountId = registration.get("id").asText();

        JsonNode tokens = customerLogin();
        var jwt = jwtDecoder.decode(tokens.get("access_token").asText());
        assertThat(jwt.getClaimAsString("principal_type")).isEqualTo("CUSTOMER");
        assertThat(jwt.getClaimAsString("principal_id")).isEqualTo(accountId);
        assertThat(jwt.getSubject()).isEqualTo(accountId);
        mockMvc.perform(get("/api/auth/customers/me")
                        .header(AUTHORIZATION, bearer(tokens.get("access_token").asText())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.id").value(Integer.valueOf(accountId)))
                .andExpect(jsonPath("$.guest.phone").value("0900000092"));
    }

    @Test
    void employeeAndCustomerTokensAndRefreshRowsRemainDistinct() throws Exception {
        registerCustomer();
        JsonNode employee = objectMapper.readTree(mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(json(new AuthDtos.LoginRequest("employee", "employee-password"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        JsonNode customer = customerLogin();

        assertThat(jwtDecoder.decode(employee.get("access_token").asText()).getClaimAsString("principal_type"))
                .isEqualTo("EMPLOYEE");
        assertThat(jwtDecoder.decode(customer.get("access_token").asText()).getClaimAsString("principal_type"))
                .isEqualTo("CUSTOMER");
        mockMvc.perform(get("/api/auth/customers/me")
                        .header(AUTHORIZATION, bearer(employee.get("access_token").asText())))
                .andExpect(status().isForbidden());
        assertThat(refreshTokens.findAll()).allSatisfy(token -> {
            assertThat(token.getPrincipalType()).isIn("EMPLOYEE", "CUSTOMER");
            if ("EMPLOYEE".equals(token.getPrincipalType())) assertThat(token.getEmployeeId()).isEqualTo("employee");
            else assertThat(token.getCustomerAccountId()).isNotNull();
        });
        assertThat(refresh(customer.get("refresh_token").asText())
                .get("access_token").asText()).isNotBlank();
        assertThat(jwtDecoder.decode(refresh(employee.get("refresh_token").asText()).get("access_token").asText())
                .getClaimAsString("principal_type")).isEqualTo("EMPLOYEE");
    }

    @Test
    void customerRefreshRotationReplayAndLogoutRevokeTheFamily() throws Exception {
        registerCustomer();
        JsonNode first = customerLogin();
        JsonNode rotated = refresh(first.get("refresh_token").asText());
        assertThat(rotated.get("refresh_token").asText()).isNotEqualTo(first.get("refresh_token").asText());
        mockMvc.perform(post("/api/auth/refresh").contentType(APPLICATION_JSON)
                        .content(json(new AuthDtos.RefreshRequest(first.get("refresh_token").asText()))))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
        assertThat(refreshTokens.findAll()).allMatch(token -> token.getRevokedAt() != null);

        JsonNode fresh = customerLogin();
        mockMvc.perform(post("/api/auth/logout")
                        .header(AUTHORIZATION, bearer(fresh.get("access_token").asText()))
                        .contentType(APPLICATION_JSON)
                        .content(json(new AuthDtos.LogoutRequest(fresh.get("refresh_token").asText()))))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/auth/refresh").contentType(APPLICATION_JSON)
                        .content(json(new AuthDtos.RefreshRequest(fresh.get("refresh_token").asText()))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void customerPasswordResetIsBoundToAuthenticatedCustomerAndRevokesSessions() throws Exception {
        registerCustomer();
        JsonNode tokens = customerLogin();
        mockMvc.perform(post("/api/auth/customers/password")
                        .header(AUTHORIZATION, bearer(tokens.get("access_token").asText()))
                        .contentType(APPLICATION_JSON)
                        .content(json(new CustomerAccountDtos.PasswordResetRequest("new-password"))))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/auth/refresh").contentType(APPLICATION_JSON)
                        .content(json(new AuthDtos.RefreshRequest(tokens.get("refresh_token").asText()))))
                .andExpect(status().isUnauthorized());
        customerLoginWithPassword("new-password");
        assertThat(jdbc.queryForObject("select count(*) from audit_logs where action = 'PASSWORD_RESET'", Integer.class))
                .isEqualTo(1);
    }

    @Test
    void customerRegistrationCannotReuseEmployeePhone() throws Exception {
        mockMvc.perform(post("/api/auth/customers/register")
                        .contentType(APPLICATION_JSON)
                        .content(json(new CustomerAccountDtos.RegisterRequest(
                                "0900000091", "customer-password", "Customer", "ID09000093"))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("PHONE_ALREADY_IN_USE"));
    }

    private void registerCustomer() throws Exception {
        mockMvc.perform(post("/api/auth/customers/register").contentType(APPLICATION_JSON)
                        .content(json(new CustomerAccountDtos.RegisterRequest(
                                "0900000092", "customer-password", "Customer", "ID09000092"))))
                .andExpect(status().isCreated());
    }

    private JsonNode customerLogin() throws Exception {
        return customerLoginWithPassword("customer-password");
    }

    private JsonNode customerLoginWithPassword(String password) throws Exception {
        return objectMapper.readTree(mockMvc.perform(post("/api/auth/customers/login")
                        .contentType(APPLICATION_JSON)
                        .content(json(new CustomerAccountDtos.LoginRequest("0900000092", password))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }

    private JsonNode refresh(String refreshToken) throws Exception {
        return objectMapper.readTree(mockMvc.perform(post("/api/auth/refresh").contentType(APPLICATION_JSON)
                        .content(json(new AuthDtos.RefreshRequest(refreshToken))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }

    private String json(Object value) throws Exception { return objectMapper.writeValueAsString(value); }
    private String bearer(String value) { return "Bearer " + value; }
}
