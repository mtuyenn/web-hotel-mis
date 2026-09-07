package com.hospitality.mis.guest.domain;

import java.math.BigDecimal;
import java.util.Objects;

/** Membership qualification policy for the guest aggregate. */
public final class MembershipPolicy {
    private final BigDecimal vipSpendThreshold;
    private final int vipStayThreshold;

    public MembershipPolicy(BigDecimal vipSpendThreshold, int vipStayThreshold) {
        this.vipSpendThreshold = Objects.requireNonNull(vipSpendThreshold, "vipSpendThreshold");
        if (vipSpendThreshold.signum() < 0) {
            throw new IllegalArgumentException("vipSpendThreshold must not be negative");
        }
        if (vipStayThreshold <= 0) {
            throw new IllegalArgumentException("vipStayThreshold must be positive");
        }
        this.vipStayThreshold = vipStayThreshold;
    }

    public static MembershipPolicy defaults() {
        return new MembershipPolicy(new BigDecimal("10000000"), 10);
    }

    public BigDecimal vipSpendThreshold() {
        return vipSpendThreshold;
    }

    public int vipStayThreshold() {
        return vipStayThreshold;
    }

    public MembershipTier tierFor(BigDecimal totalSpend, long completedStays) {
        if (completedStays < 0) {
            throw new IllegalArgumentException("completedStays must not be negative");
        }
        boolean qualifiesBySpend = totalSpend != null
                && totalSpend.compareTo(vipSpendThreshold) >= 0;
        return qualifiesBySpend || completedStays >= vipStayThreshold
                ? MembershipTier.VIP
                : MembershipTier.STANDARD;
    }
}
