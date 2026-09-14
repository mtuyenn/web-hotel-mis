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



/** Bảo vệ bảng giá: minimum, làm tròn, phụ phí trễ, membership và compensation. */
class PricingPolicyTest {

    private final PricingPolicy policy = new PricingPolicy(3, 20, new BigDecimal("10"));

    private final LocalDateTime start = LocalDateTime.of(2026, 1, 1, 10, 0);



    /** Given thuê theo giờ 181 phút, When tính tiền, Then áp minimum 3 giờ và làm tròn lên. */
    @Test void hourlyStayUsesThreeHourMinimumAndRoundsUp() {

        assertEquals(new BigDecimal("400.00"), policy.roomCharge(new BigDecimal("2400"), start, start.plusMinutes(181), true));

    }

    /** Given price null hoặc interval đảo chiều, When định giá, Then ném domain error thay vì trả zero. */
    @Test void invalidPricingInputIsRejectedInsteadOfPricedAsZero() {
        assertThrows(DomainException.class, () -> policy.roomCharge(null, start, start.plusHours(1), true));
        assertThrows(DomainException.class, () -> policy.roomCharge(new BigDecimal("1000"), start.plusHours(1), start, true));
    }



    /** Given package 25 giờ, When tính tiền, Then làm tròn thành hai ngày thay vì tính lẻ. */
    @Test void packageStayOfMoreThanOneDayRoundsToNextDay() {

        assertEquals(new BigDecimal("4800"), policy.roomCharge(new BigDecimal("2400"), start, start.plusHours(25), false));

    }

    /** Given hourly rental 25 giờ, When tính tiền, Then vẫn theo hourly rule, không đổi sang daily. */
    @Test void hourlyRentalDoesNotSilentlySwitchToDailyPricing() {

        assertEquals(new BigDecimal("2500.00"), policy.roomCharge(new BigDecimal("2400"), start, start.plusHours(25), true));

    }



    /** Given trễ 20 phút trong grace, When tính surcharge, Then không thu thêm. */
    @Test void lateCheckoutWithinGraceIsFree() {

        assertEquals(BigDecimal.ZERO, policy.lateSurcharge(new BigDecimal("1000"), start.plusHours(10), start.plusHours(10).plusMinutes(20)));

    }

    /** Given các mốc 21, 121 và 241 phút, When tính phụ phí, Then ranh giới phút được bảo toàn. */
    @Test void lateCheckoutUsesMinuteBoundaries() {
        LocalDateTime planned = LocalDateTime.of(2026, 1, 2, 12, 0);
        assertEquals(new BigDecimal("150.00"), policy.lateSurcharge(new BigDecimal("1000"), planned,
                LocalDateTime.of(2026, 1, 2, 12, 21)));
        assertEquals(new BigDecimal("200.00"), policy.lateSurcharge(new BigDecimal("1000"), planned,
                LocalDateTime.of(2026, 1, 2, 14, 1)));
        assertEquals(new BigDecimal("500.00"), policy.lateSurcharge(new BigDecimal("1000"), planned,
                LocalDateTime.of(2026, 1, 2, 16, 1)));
    }

    /** Given checkout sau nửa đêm, When trễ quá một ngày, Then surcharge bị chặn ở 100%. */
    @Test void lateCheckoutAfterMidnightChargesOneHundredPercent() {
        LocalDateTime planned = LocalDateTime.of(2026, 1, 2, 12, 0);
        assertEquals(new BigDecimal("1000.00"), policy.lateSurcharge(new BigDecimal("1000"), planned,
                LocalDateTime.of(2026, 1, 3, 0, 1)));
    }

    /** Given hai giá trị quanh ngưỡng nghìn, When round, Then chỉ round nearest thousand theo half-up. */
    @Test void finalTotalRoundsOnlyToNearestThousand() {
        assertEquals(new BigDecimal("1251000.00"), policy.roundFinalTotal(new BigDecimal("1250500")));
        assertEquals(new BigDecimal("1250000.00"), policy.roundFinalTotal(new BigDecimal("1250499")));
    }



    /** Given spend 1000 ở Silver/Gold, When tính discount, Then lần lượt là 5% và 10%. */
    @Test void vipDiscountIsTenPercent() {

        assertEquals(new BigDecimal("50.00"), policy.vipDiscount(new BigDecimal("1000"), MembershipTier.SILVER));
        assertEquals(new BigDecimal("100.00"), policy.vipDiscount(new BigDecimal("1000"), MembershipTier.GOLD));

    }



    /** Given thiết bị mới/cũ, When bồi thường, Then áp đúng tỷ lệ theo tuổi. */
    @Test void equipmentCompensationUsesAgeRate() {

        assertEquals(new BigDecimal("1500.00"), policy.equipmentCompensation(new BigDecimal("1000"),

                LocalDate.of(2025, 1, 1), 1, LocalDate.of(2026, 1, 1)));

        assertEquals(new BigDecimal("2000.00"), policy.equipmentCompensation(new BigDecimal("1000"),

                LocalDate.of(2022, 1, 1), 1, LocalDate.of(2026, 1, 1)));

    }

    /** Given đúng và quá mốc 2 năm, When tính rate, Then chuyển ngay sau anniversary và chặn ngày tương lai. */
    @Test void equipmentRateChangesImmediatelyAfterSecondAnniversary() {
        LocalDate purchased = LocalDate.of(2024, 1, 1);
        assertEquals(new BigDecimal("1500.00"), policy.equipmentCompensation(new BigDecimal("1000"), purchased, 1, purchased.plusYears(2)));
        assertEquals(new BigDecimal("2000.00"), policy.equipmentCompensation(new BigDecimal("1000"), purchased, 1, purchased.plusYears(2).plusDays(1)));
        assertThrows(DomainException.class, () -> policy.equipmentCompensation(new BigDecimal("1000"), purchased, 1, purchased.minusDays(1)));
    }

}
