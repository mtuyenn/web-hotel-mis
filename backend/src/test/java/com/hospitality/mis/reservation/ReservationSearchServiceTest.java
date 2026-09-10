package com.hospitality.mis.reservation;

import com.hospitality.mis.dao.reservation.ReservationRepository;
import com.hospitality.mis.service.reservation.ReservationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.assertj.core.api.Assertions.*;

class ReservationSearchServiceTest {
    private final ReservationRepository repository = mock(ReservationRepository.class);
    private final ReservationService service = new ReservationService(repository, null, null, null, null, null, null, null, null);
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }
    @Test void frontDeskCanFindPreviousShiftBookings() {
        authenticate("clerk", "FRONT_DESK");
        when(repository.searchIds(isNull(), isNull(), isNull(), any())).thenReturn(Page.empty());
        assertThat(service.list(null, null, 0, 20).totalElements()).isZero();
        verify(repository).searchIds(isNull(), isNull(), isNull(), any());
        verify(repository, never()).findPageDetails(any());
    }
    @Test void managerHasGlobalScope() {
        authenticate("manager", "MANAGER");
        when(repository.searchIds(isNull(), isNull(), isNull(), any())).thenReturn(Page.empty());
        service.list(null, null, 0, 20);
        verify(repository).searchIds(isNull(), isNull(), isNull(), any());
    }
    @Test void invalidBoundsAndUnauthenticatedQueriesDoNotReachDatabase() {
        assertThatThrownBy(() -> service.list(null, null, 0, 20))
                .isInstanceOf(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class);
        authenticate("clerk", "FRONT_DESK");
        assertThatThrownBy(() -> service.list(null, null, -1, 20)).extracting("code").isEqualTo("INVALID_SEARCH");
        assertThatThrownBy(() -> service.list(null, null, 0, 101)).extracting("code").isEqualTo("INVALID_SEARCH");
        assertThatThrownBy(() -> service.list(null, 0L, 0, 20)).extracting("code").isEqualTo("INVALID_SEARCH");
        verifyNoInteractions(repository);
    }
    private void authenticate(String actor, String role) {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(actor, "", "ROLE_" + role));
    }
}
