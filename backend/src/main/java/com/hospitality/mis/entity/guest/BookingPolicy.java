package com.hospitality.mis.entity.guest;



import java.util.Objects;



/** Booking eligibility policy owned by the guest bounded context. */

public final class BookingPolicy {

    public static final int DEFAULT_BLOCK_AT_LATE_CANCELLATION_COUNT = 4;



    private final int blockAtLateCancellationCount;



    public BookingPolicy(int blockAtLateCancellationCount) {

        if (blockAtLateCancellationCount <= 0) {

            throw new IllegalArgumentException("blockAtLateCancellationCount must be positive");

        }

        this.blockAtLateCancellationCount = blockAtLateCancellationCount;

    }



    public static BookingPolicy defaults() {

        return new BookingPolicy(DEFAULT_BLOCK_AT_LATE_CANCELLATION_COUNT);

    }



    public int blockAtLateCancellationCount() {

        return blockAtLateCancellationCount;

    }



    public boolean allows(Guest guest) {

        return !Objects.requireNonNull(guest, "guest").isBookingBlocked();

    }



    public void recordLateCancellation(Guest guest) {

        Objects.requireNonNull(guest, "guest")

                .recordLateCancellation(blockAtLateCancellationCount);

    }

}
