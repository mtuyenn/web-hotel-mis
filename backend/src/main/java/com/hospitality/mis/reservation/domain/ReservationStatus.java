package com.hospitality.mis.reservation.domain;

public enum ReservationStatus {
    DRAFT,
    DEPOSIT_PAID,
    CONFIRMED,
    CHECKED_IN,
    CHECKED_OUT,
    CANCELLED,
    NO_SHOW;

    public boolean canTransitionTo(ReservationStatus next) {
        return switch (this) {
            case DRAFT -> next == CONFIRMED || next == DEPOSIT_PAID || next == CANCELLED;
            case DEPOSIT_PAID, CONFIRMED -> next == CHECKED_IN || next == CANCELLED || next == NO_SHOW;
            case CHECKED_IN -> next == CHECKED_OUT;
            case CHECKED_OUT, CANCELLED, NO_SHOW -> false;
        };
    }
}
