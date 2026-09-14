package com.hospitality.mis.entity.guest;

/** Chính sách xét hạng thành viên chỉ dựa trên số lần lưu trú đã hoàn tất. */
public final class MembershipPolicy {
    /** Ngưỡng số lượt lưu trú để đạt hạng SILVER. */
    private final int silverStayThreshold;
    /** Ngưỡng số lượt lưu trú để đạt hạng GOLD. */
    private final int goldStayThreshold;
    /** Ngưỡng số lượt lưu trú để đạt hạng PLATINUM. */
    private final int platinumStayThreshold;

    /** Tạo policy; các ngưỡng phải tăng dần và đều dương. */
    public MembershipPolicy(int silverStayThreshold, int goldStayThreshold, int platinumStayThreshold) {
        if (silverStayThreshold <= 0 || goldStayThreshold <= silverStayThreshold
                || platinumStayThreshold <= goldStayThreshold) {
            throw new IllegalArgumentException("membership stay thresholds must be ascending and positive");
        }
        this.silverStayThreshold = silverStayThreshold;
        this.goldStayThreshold = goldStayThreshold;
        this.platinumStayThreshold = platinumStayThreshold;
    }

    /** Tạo policy mặc định với các ngưỡng hiện hành. */
    public static MembershipPolicy defaults() { return new MembershipPolicy(10, 25, 50); }

    /** Ánh xạ số lượt lưu trú hoàn tất sang hạng thành viên cao nhất đạt được. */
    public MembershipTier tierFor(long completedStays) {
        if (completedStays < 0) throw new IllegalArgumentException("completedStays must not be negative");
        if (completedStays >= platinumStayThreshold) return MembershipTier.PLATINUM;
        if (completedStays >= goldStayThreshold) return MembershipTier.GOLD;
        if (completedStays >= silverStayThreshold) return MembershipTier.SILVER;
        return MembershipTier.STANDARD;
    }
}
