package com.hospitality.mis.entity.operations;

public enum TechnicalWorkOrderStatus {
    NEW, ACKNOWLEDGED, IN_PROGRESS, WAITING_ACCEPTANCE, COMPLETED, ROOM_RELEASED;

    public boolean canTransitionTo(TechnicalWorkOrderStatus next) {
        if (this == next) return true;
        return switch (this) {
            case NEW -> next == ACKNOWLEDGED;
            case ACKNOWLEDGED -> next == IN_PROGRESS;
            case IN_PROGRESS -> next == WAITING_ACCEPTANCE;
            case WAITING_ACCEPTANCE -> next == COMPLETED;
            case COMPLETED -> next == ROOM_RELEASED;
            case ROOM_RELEASED -> false;
        };
    }
}
