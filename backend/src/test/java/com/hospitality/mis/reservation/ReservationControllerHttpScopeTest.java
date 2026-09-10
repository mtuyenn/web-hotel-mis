package com.hospitality.mis.reservation;
import com.hospitality.mis.entity.reservation.ReservationStatus;


import com.hospitality.mis.service.operations.EquipmentIncidentService;
import com.hospitality.mis.dto.reservation.ReservationDtos;
import com.hospitality.mis.service.reservation.ReservationService;
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
        "spring.datasource.url=jdbc:h2:mem:reservationcontrollerscope;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class ReservationControllerHttpScopeTest {
    @Autowired MockMvc mockMvc;

    @MockBean ReservationService service;
    @MockBean EquipmentIncidentService incidents;

    @BeforeEach
    void stubReservation() {
        when(service.get(7L)).thenReturn(responseOwnedBy("frontdesk"));
    }

    @Test
    @WithMockUser(username = "frontdesk", roles = "FRONT_DESK")
    void listBindsSnakeCaseFiltersAndReturnsPageMetadata() throws Exception {
        when(service.list(ReservationStatus.CONFIRMED, 11L, 1, 10))
                .thenReturn(new ReservationDtos.PageResponse(List.of(responseOwnedBy("frontdesk")), 1, 10, 11, 2));
        mockMvc.perform(get("/api/reservations").param("status", "CONFIRMED")
                        .param("guest_id", "11").param("page", "1").param("size", "10"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].guest_id").value(11))
                .andExpect(jsonPath("$.total_elements").value(11)).andExpect(jsonPath("$.total_pages").value(2));
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void customerCannotReadStaffReservationList() throws Exception {
        mockMvc.perform(get("/api/reservations")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "frontdesk", roles = "FRONT_DESK")
    void ownerReadSucceedsOverHttp() throws Exception {
        mockMvc.perform(get("/api/reservations/{id}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.employee_id").value("frontdesk"));
    }

    @Test
    @WithMockUser(username = "other", roles = "FRONT_DESK")
    void nextShiftCanReadReservation() throws Exception {
        mockMvc.perform(get("/api/reservations/{id}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employee_id").value("frontdesk"));
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void managerHasGlobalRead() throws Exception {
        assertGlobalRead("manager");
    }

    @Test
    @WithMockUser(username = "director", roles = "DIRECTOR")
    void directorHasGlobalRead() throws Exception {
        assertGlobalRead("director");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminHasGlobalRead() throws Exception {
        assertGlobalRead("admin");
    }

    private void assertGlobalRead(String actor) throws Exception {
        mockMvc.perform(get("/api/reservations/{id}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employee_id").value("frontdesk"));
    }

    private static ReservationDtos.Response responseOwnedBy(String employeeId) {
        return new ReservationDtos.Response(7L, 11L, employeeId,
                com.hospitality.mis.entity.reservation.ReservationStatus.CONFIRMED,
                ReservationDtos.RentalType.PACKAGE, BigDecimal.ZERO, null, null, null, List.of());
    }
}
