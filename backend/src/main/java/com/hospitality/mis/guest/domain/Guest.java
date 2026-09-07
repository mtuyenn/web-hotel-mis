package com.hospitality.mis.guest.domain;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Canonical guest state and persistence mapping.
 *
 * The sole concrete JPA owner of the {@code guests} table.
 */
@Entity
@Table(name = "guests")
@Access(AccessType.FIELD)
public class Guest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "phone", nullable = false, unique = true, length = 15)
    private String phone;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "identity_number", nullable = false, unique = true, length = 12)
    private String identityNumber;

    @Column(name = "birth_year")
    private Integer birthYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_tier", nullable = false, length = 20)
    private MembershipTier membershipTier = MembershipTier.STANDARD;

    @Column(name = "total_spend", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalSpend = BigDecimal.ZERO;

    @Column(name = "late_cancellation_count", nullable = false)
    private int lateCancellationCount;

    @Column(name = "booking_blocked", nullable = false)
    private boolean bookingBlocked;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public Guest() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIdentityNumber() {
        return identityNumber;
    }

    public void setIdentityNumber(String identityNumber) {
        this.identityNumber = identityNumber;
    }

    public Integer getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(Integer birthYear) {
        this.birthYear = birthYear;
    }

    public MembershipTier getMembershipTier() {
        return membershipTier;
    }

    public void setMembershipTier(MembershipTier membershipTier) {
        this.membershipTier = Objects.requireNonNull(membershipTier, "membershipTier");
    }

    public BigDecimal getTotalSpend() {
        return totalSpend;
    }

    public void setTotalSpend(BigDecimal totalSpend) {
        this.totalSpend = totalSpend == null ? BigDecimal.ZERO : totalSpend;
    }

    public int getLateCancellationCount() {
        return lateCancellationCount;
    }

    public void setLateCancellationCount(int lateCancellationCount) {
        if (lateCancellationCount < 0) {
            throw new IllegalArgumentException("lateCancellationCount must not be negative");
        }
        this.lateCancellationCount = lateCancellationCount;
    }

    public boolean isBookingBlocked() {
        return bookingBlocked;
    }

    public void setBookingBlocked(boolean bookingBlocked) {
        this.bookingBlocked = bookingBlocked;
    }

    public long getVersion() {
        return version;
    }

    /** Returns whether this guest may be used as the guest on a new booking. */
    public boolean canPlaceBooking() {
        return !bookingBlocked;
    }

    /**
     * Records a late cancellation and applies the booking-block threshold.
     * The reservation context currently owns its cancellation use case; this
     * method keeps the rule available in the canonical guest model without
     * changing that context's behavior during the migration.
     */
    public void recordLateCancellation(int blockAtCount) {
        if (blockAtCount <= 0) {
            throw new IllegalArgumentException("blockAtCount must be positive");
        }
        lateCancellationCount++;
        if (lateCancellationCount >= blockAtCount) {
            bookingBlocked = true;
        }
    }

    /** Adds a settled amount to the guest's lifetime spend. */
    public void addSpend(BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        totalSpend = totalSpend.add(amount);
    }

    /** Applies the configured membership policy to the current guest state. */
    public void refreshMembership(MembershipPolicy policy, long completedStays) {
        membershipTier = Objects.requireNonNull(policy, "policy")
                .tierFor(totalSpend, completedStays);
    }
}
