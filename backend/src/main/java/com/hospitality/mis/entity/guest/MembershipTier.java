package com.hospitality.mis.entity.guest;



public enum MembershipTier {

    STANDARD(0),
    SILVER(5),
    GOLD(10),
    PLATINUM(15);

    private final int discountPercent;

    MembershipTier(int discountPercent) { this.discountPercent = discountPercent; }

    public int discountPercent() { return discountPercent; }

}
