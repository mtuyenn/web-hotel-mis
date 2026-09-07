package com.hospitality.mis.billing;

import com.hospitality.mis.billing.application.PricingPolicy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingPolicyTest {
    private final PricingPolicy policy = new PricingPolicy(3, 20, new BigDecimal("10"));
    private final LocalDateTime start = LocalDateTime.of(2026, 1, 1, 10, 0);

    @Test void hourlyStayUsesThreeHourMinimumAndRoundsUp() {
        assertEquals(new BigDecimal("400.00"), policy.roomCharge(new BigDecimal("2400"), start, start.plusMinutes(181), true));
    }

    @Test void packageStayOfMoreThanOneDayRoundsToNextDay() {
        assertEquals(new BigDecimal("4800"), policy.roomCharge(new BigDecimal("2400"), start, start.plusHours(25), false));
    }

    @Test void lateCheckoutWithinGraceIsFree() {
        assertEquals(BigDecimal.ZERO, policy.lateSurcharge(new BigDecimal("1000"), start.plusHours(10), start.plusHours(10).plusMinutes(20)));
    }

    @Test void vipDiscountIsTenPercent() {
        assertEquals(new BigDecimal("100.00"), policy.vipDiscount(new BigDecimal("1000"), true));
    }

    @Test void equipmentCompensationUsesAgeRate() {
        assertEquals(new BigDecimal("1500.00"), policy.equipmentCompensation(new BigDecimal("1000"),
                LocalDate.of(2025, 1, 1), 1, LocalDate.of(2026, 1, 1)));
        assertEquals(new BigDecimal("2000.00"), policy.equipmentCompensation(new BigDecimal("1000"),
                LocalDate.of(2022, 1, 1), 1, LocalDate.of(2026, 1, 1)));
    }
}
