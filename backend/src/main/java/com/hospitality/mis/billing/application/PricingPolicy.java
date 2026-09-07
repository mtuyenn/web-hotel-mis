package com.hospitality.mis.billing.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Pure policy object: calculations have no database or time side effects. */
@Component
public class PricingPolicy {
    private final int hourlyMinimum;
    private final int graceMinutes;
    private final BigDecimal vipDiscount;

    public PricingPolicy(@Value("${hotel.policy.hourly-minimum:3}") int hourlyMinimum,
                         @Value("${hotel.policy.late-checkout-grace-minutes:20}") int graceMinutes,
                         @Value("${hotel.policy.vip-discount-percent:10}") BigDecimal vipDiscount) {
        this.hourlyMinimum = hourlyMinimum; this.graceMinutes = graceMinutes; this.vipDiscount = vipDiscount;
    }

    public BigDecimal roomCharge(BigDecimal dailyPrice, LocalDateTime from, LocalDateTime to, boolean hourly) {
        if (dailyPrice == null || from == null || to == null || !from.isBefore(to)) return BigDecimal.ZERO;
        long minutes = Math.max(1, Duration.between(from, to).toMinutes());
        if (!hourly || minutes >= 18 * 60) {
            long days = Math.max(1, (minutes + (24 * 60) - 1) / (24 * 60));
            return dailyPrice.multiply(BigDecimal.valueOf(days));
        }
        long hours = Math.max(hourlyMinimum, (minutes + 59) / 60);
        BigDecimal hourlyPrice = dailyPrice.divide(BigDecimal.valueOf(24), 2, RoundingMode.HALF_UP);
        return hourlyPrice.multiply(BigDecimal.valueOf(hours));
    }

    public BigDecimal extensionCharge(BigDecimal dailyPrice, int extensionMinutes) {
        if (dailyPrice == null || extensionMinutes <= 0) return BigDecimal.ZERO;
        BigDecimal hourly = dailyPrice.divide(BigDecimal.valueOf(24), 2, RoundingMode.HALF_UP);
        long hours = (extensionMinutes + 59L) / 60L;
        return hourly.multiply(BigDecimal.valueOf(hours));
    }

    public BigDecimal lateSurcharge(BigDecimal roomTotal, LocalDateTime plannedCheckout, LocalDateTime actualCheckout) {
        if (roomTotal == null || plannedCheckout == null || actualCheckout == null
                || !actualCheckout.isAfter(plannedCheckout.plusMinutes(graceMinutes))) return BigDecimal.ZERO;
        int hour = actualCheckout.getHour();
        BigDecimal rate = hour <= 14 ? new BigDecimal("0.15")
                : hour <= 16 ? new BigDecimal("0.20")
                : hour <= 18 ? new BigDecimal("0.50") : BigDecimal.ONE;
        return roomTotal.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal vipDiscount(BigDecimal subtotal, boolean vip) {
        if (!vip || subtotal == null) return BigDecimal.ZERO;
        return subtotal.multiply(vipDiscount).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal equipmentCompensation(BigDecimal originalValue, LocalDate purchasedAt, int quantity,
                                            LocalDate referenceDate) {
        if (originalValue == null || purchasedAt == null || quantity <= 0 || referenceDate == null) return BigDecimal.ZERO;
        long age = Math.max(0, ChronoUnit.YEARS.between(purchasedAt, referenceDate));
        BigDecimal multiplier = age <= 2 ? new BigDecimal("1.50") : new BigDecimal("2.00");
        return originalValue.multiply(multiplier).multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }
}
