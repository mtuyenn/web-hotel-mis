package com.hospitality.mis.guest;



import com.hospitality.mis.dao.guest.GuestRepository;
import com.hospitality.mis.entity.guest.Guest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;


import static org.springframework.http.MediaType.APPLICATION_JSON;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@SpringBootTest(properties = {

        "spring.datasource.url=jdbc:h2:mem:guestapi;MODE=MySQL;DB_CLOSE_DELAY=-1",

        "spring.datasource.username=sa",

        "spring.datasource.password=",

        "spring.flyway.enabled=false",

        "spring.jpa.hibernate.ddl-auto=create-drop"

})

@AutoConfigureMockMvc
class GuestApiContractTest {
    @Autowired MockMvc mockMvc;
    @Autowired GuestRepository guests;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void cleanGuests() {
        jdbc.update("delete from audit_logs");
        guests.deleteAllInBatch();
    }

    @Test
    @WithMockUser(username = "frontdesk", roles = "FRONT_DESK")
    void createUsesLowerSnakeCaseJsonAndBindsAuditToAuthenticatedActor() throws Exception {
        mockMvc.perform(post("/api/guests")
                        .header("X-Actor-Id", "manager")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "full_name":"Nguyen Van An",
                                  "birth_year":1990,
                                  "identity_number":"012345678901",
                                  "phone":"0901234567",
                                  "email":"an@example.test",
                                  "address":"Quan 1"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.full_name").value("Nguyen Van An"))
                .andExpect(jsonPath("$.birth_year").value(1990))
                .andExpect(jsonPath("$.identity_number").value("012345678901"))
                .andExpect(jsonPath("$.phone").value("0901234567"))
                .andExpect(jsonPath("$.membership_tier").value("STANDARD"))
                .andExpect(jsonPath("$.total_spend").value(0))
                .andExpect(jsonPath("$.late_cancellation_count").value(0))
                .andExpect(jsonPath("$.booking_blocked").value(false));

        String actor = jdbc.queryForObject(
                "select actor from audit_logs where action = 'GUEST_CREATED' order by id desc limit 1",
                String.class);
        org.assertj.core.api.Assertions.assertThat(actor).isEqualTo("frontdesk");
    }

    @Test
    @WithMockUser(username = "frontdesk", roles = "FRONT_DESK")
    void frontDeskSearchUsesLowerSnakeCaseResponseFields() throws Exception {
        Guest guest = new Guest();
        guest.setFullName("Tran Thi Binh");
        guest.setBirthYear(1988);
        guest.setPhone("0912345678");
        guest.setEmail("binh@example.test");
        guest.setAddress("Quan 1");
        guest.setIdentityNumber("098765432109");
        guests.saveAndFlush(guest);

        mockMvc.perform(get("/api/guests").param("q", "binh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].full_name").value("Tran Thi Binh"))
                .andExpect(jsonPath("$[0].birth_year").value(1988))
                .andExpect(jsonPath("$[0].identity_number").value("098765432109"))
                .andExpect(jsonPath("$[0].phone").value("0912345678"))
                .andExpect(jsonPath("$[0].email").value("binh@example.test"))
                .andExpect(jsonPath("$[0].address").value("Quan 1"))
                .andExpect(jsonPath("$[0].membership_tier").value("STANDARD"))
                .andExpect(jsonPath("$[0].total_spend").value(0))
                .andExpect(jsonPath("$[0].late_cancellation_count").value(0))
                .andExpect(jsonPath("$[0].booking_blocked").value(false));
    }

    @Test
    void anonymousGuestReadReturnsStableAuthenticationError() throws Exception {
        mockMvc.perform(get("/api/guests"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    @WithMockUser(username = "frontdesk", roles = "FRONT_DESK")
    void missingGuestReturnsStableDomainError() throws Exception {
        mockMvc.perform(get("/api/guests/{id}", 999L))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.code").value("GUEST_NOT_FOUND"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details").isEmpty());
    }

    @Test
    @WithMockUser(username = "frontdesk", roles = "FRONT_DESK")
    void invalidGuestCreateReturnsStableValidationError() throws Exception {
        mockMvc.perform(post("/api/guests")
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details").isArray());
    }
}
