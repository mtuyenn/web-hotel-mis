package com.hospitality.mis.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospitality.mis.dao.auth.RefreshTokenRepository;
import com.hospitality.mis.dao.identity.EmployeeRepository;
import com.hospitality.mis.dto.auth.AuthDtos;
import com.hospitality.mis.entity.identity.Employee;
import com.hospitality.mis.entity.identity.EmployeeRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Security smoke checks against real MySQL without dropping the QLKS schema. */
@SpringBootTest(properties = {
        "spring.datasource.url=${MIGRATION_TEST_DB_URL}",
        "spring.datasource.username=${MIGRATION_TEST_DB_USERNAME}",
        "spring.datasource.password=${MIGRATION_TEST_DB_PASSWORD}",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "MIGRATION_TEST_DB_URL", matches = ".+")
class MySqlSecuritySmokeTest {
    private static final String EMPLOYEE_ID = "msec001";
    private static final String PHONE = "0999999001";
    private static final String PASSWORD = "mysql-sec-password";

    @Autowired MockMvc mvc;
    @Autowired EmployeeRepository employees;
    @Autowired RefreshTokenRepository refreshTokens;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired ObjectMapper objectMapper;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void seed() {
        refreshTokens.deleteAll();
        jdbc.update("delete from audit_logs where actor = ?", EMPLOYEE_ID);
        employees.findById(EMPLOYEE_ID).ifPresent(employees::delete);
        Employee employee = new Employee();
        employee.setEmployeeId(EMPLOYEE_ID);
        employee.setFullName("MySQL Security Test");
        employee.setPassword(passwordEncoder.encode(PASSWORD));
        employee.setPhone(PHONE);
        employee.setRole(EmployeeRole.FRONT_DESK);
        employees.saveAndFlush(employee);
    }

    @AfterEach
    void cleanup() {
        refreshTokens.deleteAll();
        jdbc.update("delete from audit_logs where actor = ?", EMPLOYEE_ID);
        employees.findById(EMPLOYEE_ID).ifPresent(employees::delete);
    }

    @Test
    void anonymousRequestIsRejectedByMySqlBackedApplication() throws Exception {
        mvc.perform(get("/api/services"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void authenticatedStaffCanReadButCannotWriteDepartmentData() throws Exception {
        JsonNode token = login();
        String bearer = "Bearer " + token.get("access_token").asText();
        mvc.perform(get("/api/services").header(AUTHORIZATION, bearer))
                .andExpect(status().isOk());
        mvc.perform(post("/api/services").header(AUTHORIZATION, bearer)
                        .contentType(APPLICATION_JSON)
                        .content("{\"id\":\"SEC1\",\"name\":\"Test\",\"price\":1,\"opening_stock\":0,\"safety_threshold\":0}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESS_DENIED"));
    }

    @Test
    void tokenIsRejectedAfterMySqlAccountIsDisabled() throws Exception {
        JsonNode token = login();
        Employee employee = employees.findById(EMPLOYEE_ID).orElseThrow();
        employee.setEnabled(false);
        employees.saveAndFlush(employee);
        mvc.perform(get("/api/services")
                        .header(AUTHORIZATION, "Bearer " + token.get("access_token").asText()))
                .andExpect(status().isUnauthorized());
    }

    private JsonNode login() throws Exception {
        return objectMapper.readTree(mvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthDtos.LoginRequest(EMPLOYEE_ID, PASSWORD))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
    }
}
