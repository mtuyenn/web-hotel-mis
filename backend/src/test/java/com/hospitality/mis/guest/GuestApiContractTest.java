package com.hospitality.mis.guest;

import com.hospitality.mis.guest.adapter.GuestRepository;
import com.hospitality.mis.guest.domain.Guest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

    @BeforeEach
    void cleanGuests() {
        guests.deleteAllInBatch();
    }

    @Test
    @WithMockUser(username = "frontdesk", roles = "FRONT_DESK")
    void createKeepsExistingRouteStatusAndJsonContract() throws Exception {
        mockMvc.perform(post("/api/guests")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName":"Nguyen Van An",
                                  "birthYear":1990,
                                  "identityNumber":"012345678901",
                                  "phone":"0901234567",
                                  "email":"an@example.test",
                                  "address":"Quan 1"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.fullName").value("Nguyen Van An"))
                .andExpect(jsonPath("$.birthYear").value(1990))
                .andExpect(jsonPath("$.identityNumber").value("012345678901"))
                .andExpect(jsonPath("$.phone").value("0901234567"))
                .andExpect(jsonPath("$.membershipTier").value("STANDARD"))
                .andExpect(jsonPath("$.totalSpend").value(0))
                .andExpect(jsonPath("$.lateCancellationCount").value(0))
                .andExpect(jsonPath("$.bookingBlocked").value(false));
    }

    @Test
    @WithMockUser(username = "staff", roles = "STAFF")
    void onlyManagerOrFrontDeskMayCreate() throws Exception {
        mockMvc.perform(post("/api/guests")
                        .contentType(APPLICATION_JSON)
                        .content("{\"fullName\":\"Guest\",\"identityNumber\":\"012345678901\",\"phone\":\"0901234567\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "staff", roles = "STAFF")
    void searchKeepsExistingRouteAndResponseFields() throws Exception {
        Guest guest = new Guest();
        guest.setFullName("Tran Thi Binh");
        guest.setPhone("0912345678");
        guest.setEmail("binh@example.test");
        guest.setIdentityNumber("098765432109");
        guests.saveAndFlush(guest);

        mockMvc.perform(get("/api/guests").param("q", "binh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("Tran Thi Binh"))
                .andExpect(jsonPath("$[0].identityNumber").value("098765432109"));
    }
}
