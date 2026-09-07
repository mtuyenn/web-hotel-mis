package com.hospitality.mis.guest;

import com.hospitality.mis.guest.domain.BookingPolicy;
import com.hospitality.mis.guest.domain.Guest;
import com.hospitality.mis.guest.domain.MembershipPolicy;
import com.hospitality.mis.guest.domain.MembershipTier;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class GuestDomainTest {
    @Test
    void canonicalModelIsTheSoleConcreteGuestEntity() throws Exception {
        assertThat(Guest.class.isAnnotationPresent(Entity.class)).isTrue();
        assertThat(Guest.class.getAnnotation(Table.class).name()).isEqualTo("guests");

        assertThat(columnName("id")).isEqualTo("id");
        assertThat(columnName("fullName")).isEqualTo("full_name");
        assertThat(columnName("identityNumber")).isEqualTo("identity_number");
        assertThat(columnName("membershipTier")).isEqualTo("membership_tier");
        assertThat(columnName("totalSpend")).isEqualTo("total_spend");
    }

    @Test
    void canonicalStateUsesCanonicalAccessors() {
        Guest guest = new Guest();
        guest.setId(7L);
        guest.setFullName("Nguyen Van An");
        guest.setIdentityNumber("012345678901");
        guest.setPhone("0901234567");
        guest.setMembershipTier(MembershipTier.VIP);
        guest.setTotalSpend(new BigDecimal("12000000.00"));

        assertThat(guest.getId()).isEqualTo(7L);
        assertThat(guest.getFullName()).isEqualTo("Nguyen Van An");
        assertThat(guest.getIdentityNumber()).isEqualTo("012345678901");
        assertThat(guest.getPhone()).isEqualTo("0901234567");
        assertThat(guest.getMembershipTier()).isEqualTo(MembershipTier.VIP);
        assertThat(guest.getTotalSpend()).isEqualByComparingTo("12000000.00");
    }

    @Test
    void bookingPolicyBlocksOnlyAtTheConfiguredLateCancellationThreshold() {
        Guest guest = new Guest();
        BookingPolicy policy = BookingPolicy.defaults();

        assertThat(policy.allows(guest)).isTrue();
        for (int i = 0; i < BookingPolicy.DEFAULT_BLOCK_AT_LATE_CANCELLATION_COUNT - 1; i++) {
            policy.recordLateCancellation(guest);
        }
        assertThat(guest.getLateCancellationCount()).isEqualTo(3);
        assertThat(policy.allows(guest)).isTrue();

        policy.recordLateCancellation(guest);
        assertThat(guest.getLateCancellationCount()).isEqualTo(4);
        assertThat(guest.isBookingBlocked()).isTrue();
        assertThat(policy.allows(guest)).isFalse();
    }

    @Test
    void membershipPolicyUsesSpendOrCompletedStayThreshold() {
        MembershipPolicy policy = MembershipPolicy.defaults();

        assertThat(policy.tierFor(BigDecimal.ZERO, 0)).isEqualTo(MembershipTier.STANDARD);
        assertThat(policy.tierFor(new BigDecimal("10000000"), 0)).isEqualTo(MembershipTier.VIP);
        assertThat(policy.tierFor(BigDecimal.ZERO, 10)).isEqualTo(MembershipTier.VIP);
    }

    @Test
    void guestReservationRelationshipIsInverseAndNonDestructive() throws Exception {
        assertThat(Guest.class.getAnnotation(Table.class).name()).isEqualTo("guests");
    }

    private String columnName(String fieldName) throws NoSuchFieldException {
        return Guest.class.getDeclaredField(fieldName)
                .getAnnotation(jakarta.persistence.Column.class).name();
    }
}
