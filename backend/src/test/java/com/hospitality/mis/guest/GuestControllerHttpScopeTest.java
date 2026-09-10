package com.hospitality.mis.guest;

import com.hospitality.mis.dto.guest.GuestDtos;
import com.hospitality.mis.service.guest.GuestService;
import com.hospitality.mis.entity.guest.MembershipTier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:guestcontrollerscope;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class GuestControllerHttpScopeTest {
    @Autowired MockMvc mockMvc;

    @MockBean GuestService service;

    @BeforeEach
    void stubSharedGuest() {
        when(service.get(7L)).thenReturn(sharedGuest());
        when(service.search("binh")).thenReturn(List.of(sharedGuest()));
    }

    @Test
    void anonymousReadReturnsStableUnauthorizedError() throws Exception {
        mockMvc.perform(get("/api/guests/{id}", 7L))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    @WithMockUser(username = "frontdesk", roles = "FRONT_DESK")
    void frontDeskCanReadSharedGuestOverHttp() throws Exception {
        mockMvc.perform(get("/api/guests/{id}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    @WithMockUser(username = "frontdesk", roles = "FRONT_DESK")
    void frontDeskCanSearchSharedGuestsOverHttp() throws Exception {
        mockMvc.perform(get("/api/guests").param("q", "binh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7));
    }

    @Test
    @WithMockUser(username = "staff", roles = "STAFF")
    void unauthorizedGuestRoleReturnsStableForbiddenError() throws Exception {
        mockMvc.perform(get("/api/guests/{id}", 7L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESS_DENIED"));
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void managerCanReadSharedGuest() throws Exception {
        assertSharedReadSucceeds();
    }

    @Test
    @WithMockUser(username = "director", roles = "DIRECTOR")
    void directorCanReadSharedGuest() throws Exception {
        assertSharedReadSucceeds();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCanReadSharedGuest() throws Exception {
        assertSharedReadSucceeds();
    }

    private void assertSharedReadSucceeds() throws Exception {
        mockMvc.perform(get("/api/guests/{id}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    private static GuestDtos.Response sharedGuest() {
        return new GuestDtos.Response(7L, "Tran Thi Binh", 1990, "098765432109",
                "0912345678", "binh@example.test", "Quan 1", MembershipTier.STANDARD,
                BigDecimal.ZERO, 0, 0, false);
    }
}
