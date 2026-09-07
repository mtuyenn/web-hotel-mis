package com.hospitality.mis.common.idempotency;

import java.util.Objects;

/** Canonical key used to make a retried write resolve to one operation. */
public record IdempotencyKey(String value) {
    public IdempotencyKey {
        value = Objects.requireNonNull(value, "idempotency key must not be null").trim();
        if (value.isEmpty()) throw new IllegalArgumentException("idempotency key must not be blank");
        if (value.length() > 100) throw new IllegalArgumentException("idempotency key must not exceed 100 characters");
    }
}
