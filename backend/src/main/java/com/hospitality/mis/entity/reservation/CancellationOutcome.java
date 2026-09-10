package com.hospitality.mis.entity.reservation;

/** Explicit settlement decision made by the reservation cancellation policy. */
public enum CancellationOutcome {
    REFUND,
    RETAIN,
    FORFEIT
}
