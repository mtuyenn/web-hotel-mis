package com.hospitality.mis.guest;

import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.guest.api.GuestDtos;
import com.hospitality.mis.guest.application.GuestService;
import com.hospitality.mis.guest.application.GuestStore;
import com.hospitality.mis.guest.domain.MembershipTier;
import com.hospitality.mis.governance.application.AuditService;
import com.hospitality.mis.guest.domain.Guest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuestServiceTest {
    @Mock GuestStore guests;
    @Mock AuditService audit;

    @Test
    void createUsesCanonicalGuestStateAndRecordsAudit() {
        Guest entity = guest(41L);
        when(guests.newGuest()).thenReturn(entity);
        when(guests.save(entity)).thenReturn(entity);

        GuestDtos.Response response = new GuestService(guests, audit).create(
                new GuestDtos.CreateRequest("  Nguyen Van An  ", 1990, "012345678901",
                        "0901234567", "  an@example.test ", "  Quan 1  "),
                "frontdesk");

        assertThat(response.id()).isEqualTo(41L);
        assertThat(response.fullName()).isEqualTo("Nguyen Van An");
        assertThat(response.email()).isEqualTo("an@example.test");
        assertThat(response.address()).isEqualTo("Quan 1");
        assertThat(response.membershipTier()).isEqualTo(MembershipTier.STANDARD);
        assertThat(response.totalSpend()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.bookingBlocked()).isFalse();
        verify(audit).record("frontdesk", "GUEST_CREATED", "GUEST", "41", null, "Nguyen Van An", null);
    }

    @Test
    void blankSearchReturnsDeterministicStoreResultsAndMissingGuestIsDomainError() {
        Guest entity = guest(42L);
        when(guests.findAll()).thenReturn(List.of(entity));
        GuestService service = new GuestService(guests, audit);

        assertThat(service.search(" ")).singleElement().satisfies(response -> {
            assertThat(response.id()).isEqualTo(42L);
            assertThat(response.fullName()).isEqualTo("Reservation Guest");
        });

        when(guests.findById(99L)).thenReturn(java.util.Optional.empty());
        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(DomainException.class)
                .extracting("code")
                .isEqualTo("GUEST_NOT_FOUND");
    }

    private Guest guest(Long id) {
        Guest entity = new Guest();
        entity.setId(id);
        entity.setFullName("Reservation Guest");
        entity.setIdentityNumber("001001001001");
        entity.setPhone("0901000001");
        entity.setEmail("guest" + id + "@example.test");
        return entity;
    }
}
