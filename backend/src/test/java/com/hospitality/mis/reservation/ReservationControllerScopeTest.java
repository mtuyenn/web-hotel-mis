package com.hospitality.mis.reservation;
import com.hospitality.mis.entity.reservation.ReservationStatus;


import com.hospitality.mis.service.operations.EquipmentIncidentService;
import com.hospitality.mis.controller.reservation.ReservationController;
import com.hospitality.mis.dto.reservation.ReservationDtos;
import com.hospitality.mis.service.reservation.ReservationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationControllerScopeTest {
    private final ReservationService service = Mockito.mock(ReservationService.class);
    private final ReservationController controller = new ReservationController(
            service, Mockito.mock(EquipmentIncidentService.class));

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ownerMayReadReservation() {
        SecurityContextHolder.getContext().setAuthentication(authentication("frontdesk", "ROLE_FRONT_DESK"));
        ReservationDtos.Response response = responseOwnedBy("frontdesk");
        Mockito.when(service.get(7L)).thenReturn(response);

        assertThat(controller.get(7L)).isSameAs(response);
    }

    @Test
    void nextShiftMayReadReservation() {
        SecurityContextHolder.getContext().setAuthentication(authentication("other", "ROLE_FRONT_DESK"));
        Mockito.when(service.get(7L)).thenReturn(responseOwnedBy("frontdesk"));

        assertThat(controller.get(7L).employeeId()).isEqualTo("frontdesk");
    }

    @Test
    void managerMayReadReservationAcrossEmployeeScopes() {
        SecurityContextHolder.getContext().setAuthentication(authentication("manager", "ROLE_MANAGER"));
        ReservationDtos.Response response = responseOwnedBy("frontdesk");
        Mockito.when(service.get(7L)).thenReturn(response);

        assertThat(controller.get(7L)).isSameAs(response);
    }

    private static TestingAuthenticationToken authentication(String actor, String role) {
        return new TestingAuthenticationToken(actor, "n/a", role);
    }

    private static ReservationDtos.Response responseOwnedBy(String employeeId) {
        return new ReservationDtos.Response(7L, 11L, employeeId,
                com.hospitality.mis.entity.reservation.ReservationStatus.CONFIRMED,
                ReservationDtos.RentalType.PACKAGE, BigDecimal.ZERO, null, null, null, List.of());
    }
}
