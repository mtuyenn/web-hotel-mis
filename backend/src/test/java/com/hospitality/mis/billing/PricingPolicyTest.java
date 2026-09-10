package com.hospitality.mis.billing;



import com.hospitality.mis.service.billing.PricingPolicy;
import com.hospitality.mis.entity.guest.MembershipTier;

import org.junit.jupiter.api.Test;



import java.math.BigDecimal;

import java.time.LocalDateTime;

import java.time.LocalDate;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.hospitality.mis.common.exception.DomainException;



class PricingPolicyTest {

    private final PricingPolicy policy = new PricingPolicy(3, 20, new BigDecimal("10"));

    private final LocalDateTime start = LocalDateTime.of(2026, 1, 1, 10, 0);



    @Test void hourlyStayUsesThreeHourMinimumAndRoundsUp() {

        assertEquals(new BigDecimal("400.00"), policy.roomCharge(new BigDecimal("2400"), start, start.plusMinutes(181), true));

    }

    @Test void invalidPricingInputIsRejectedInsteadOfPricedAsZero() {
        assertThrows(DomainException.class, () -> policy.roomCharge(null, start, start.plusHours(1), true));
        assertThrows(DomainException.class, () -> policy.roomCharge(new BigDecimal("1000"), start.plusHours(1), start, true));
    }



    @Test void packageStayOfMoreThanOneDayRoundsToNextDay() {

        assertEquals(new BigDecimal("4800"), policy.roomCharge(new BigDecimal("2400"), start, start.plusHours(25), false));

    }

    @Test void hourlyRentalDoesNotSilentlySwitchToDailyPricing() {

        assertEquals(new BigDecimal("2500.00"), policy.roomCharge(new BigDecimal("2400"), start, start.plusHours(25), true));

    }



    @Test void lateCheckoutWithinGraceIsFree() {

        assertEquals(BigDecimal.ZERO, policy.lateSurcharge(new BigDecimal("1000"), start.plusHours(10), start.plusHours(10).plusMinutes(20)));

    }

    @Test void lateCheckoutUsesMinuteBoundaries() {
        LocalDateTime planned = LocalDateTime.of(2026, 1, 2, 12, 0);
        assertEquals(new BigDecimal("150.00"), policy.lateSurcharge(new BigDecimal("1000"), planned,
                LocalDateTime.of(2026, 1, 2, 12, 21)));
        assertEquals(new BigDecimal("200.00"), policy.lateSurcharge(new BigDecimal("1000"), planned,
                LocalDateTime.of(2026, 1, 2, 14, 1)));
        assertEquals(new BigDecimal("500.00"), policy.lateSurcharge(new BigDecimal("1000"), planned,
                LocalDateTime.of(2026, 1, 2, 16, 1)));
    }

    @Test void lateCheckoutAfterMidnightChargesOneHundredPercent() {
        LocalDateTime planned = LocalDateTime.of(2026, 1, 2, 12, 0);
        assertEquals(new BigDecimal("1000.00"), policy.lateSurcharge(new BigDecimal("1000"), planned,
                LocalDateTime.of(2026, 1, 3, 0, 1)));
    }

    @Test void finalTotalRoundsOnlyToNearestThousand() {
        assertEquals(new BigDecimal("1251000.00"), policy.roundFinalTotal(new BigDecimal("1250500")));
        assertEquals(new BigDecimal("1250000.00"), policy.roundFinalTotal(new BigDecimal("1250499")));
    }



    @Test void vipDiscountIsTenPercent() {

        assertEquals(new BigDecimal("50.00"), policy.vipDiscount(new BigDecimal("1000"), MembershipTier.SILVER));
        assertEquals(new BigDecimal("100.00"), policy.vipDiscount(new BigDecimal("1000"), MembershipTier.GOLD));

    }



    @Test void equipmentCompensationUsesAgeRate() {

        assertEquals(new BigDecimal("1500.00"), policy.equipmentCompensation(new BigDecimal("1000"),

                LocalDate.of(2025, 1, 1), 1, LocalDate.of(2026, 1, 1)));

        assertEquals(new BigDecimal("2000.00"), policy.equipmentCompensation(new BigDecimal("1000"),

                LocalDate.of(2022, 1, 1), 1, LocalDate.of(2026, 1, 1)));

    }

    @Test void equipmentRateChangesImmediatelyAfterSecondAnniversary() {
        LocalDate purchased = LocalDate.of(2024, 1, 1);
        assertEquals(new BigDecimal("1500.00"), policy.equipmentCompensation(new BigDecimal("1000"), purchased, 1, purchased.plusYears(2)));
        assertEquals(new BigDecimal("2000.00"), policy.equipmentCompensation(new BigDecimal("1000"), purchased, 1, purchased.plusYears(2).plusDays(1)));
        assertThrows(DomainException.class, () -> policy.equipmentCompensation(new BigDecimal("1000"), purchased, 1, purchased.minusDays(1)));
    }

}
