package com.hospitality.mis.entity.guest;

/** Membership qualification policy based only on completed stays. */
public final class MembershipPolicy {
    private final int silverStayThreshold;
    private final int goldStayThreshold;
    private final int platinumStayThreshold;

    public MembershipPolicy(int silverStayThreshold, int goldStayThreshold, int platinumStayThreshold) {
        if (silverStayThreshold <= 0 || goldStayThreshold <= silverStayThreshold
                || platinumStayThreshold <= goldStayThreshold) {
            throw new IllegalArgumentException("membership stay thresholds must be ascending and positive");
        }
        this.silverStayThreshold = silverStayThreshold;
        this.goldStayThreshold = goldStayThreshold;
        this.platinumStayThreshold = platinumStayThreshold;
    }

    public static MembershipPolicy defaults() { return new MembershipPolicy(10, 25, 50); }

    public MembershipTier tierFor(long completedStays) {
        if (completedStays < 0) throw new IllegalArgumentException("completedStays must not be negative");
        if (completedStays >= platinumStayThreshold) return MembershipTier.PLATINUM;
        if (completedStays >= goldStayThreshold) return MembershipTier.GOLD;
        if (completedStays >= silverStayThreshold) return MembershipTier.SILVER;
        return MembershipTier.STANDARD;
    }
}
