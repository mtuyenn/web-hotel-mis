package com.hospitality.mis.entity.operations;

/** Vòng đời dọn phòng; READY chỉ đạt được sau checklist và kiểm tra blocking. */
public enum HousekeepingTaskStatus {
    NEEDS_CLEANING, IN_PROGRESS, CLEANED, READY, WAITING_TECHNICAL;

    public boolean canTransitionTo(HousekeepingTaskStatus next) {
        if (this == next) return true;
        return switch (this) {
            case NEEDS_CLEANING -> next == IN_PROGRESS || next == WAITING_TECHNICAL;
            case IN_PROGRESS -> next == CLEANED || next == WAITING_TECHNICAL;
            case CLEANED -> next == READY || next == WAITING_TECHNICAL;
            case WAITING_TECHNICAL -> next == IN_PROGRESS || next == CLEANED;
            case READY -> false;
        };
    }
}
