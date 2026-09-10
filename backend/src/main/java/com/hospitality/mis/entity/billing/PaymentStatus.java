package com.hospitality.mis.entity.billing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentStatus {
    DA_THANH_TOAN, CHUA_THANH_TOAN, DU_KIEN;

    @JsonCreator
    public static PaymentStatus from(String value) {
        return value == null ? null : valueOf(value.trim().toUpperCase());
    }

    @JsonValue
    public String value() { return name(); }
}
