package com.hospitality.mis.common.idempotency;

import java.util.Objects;

/** Khóa chuẩn giúp một yêu cầu ghi được thử lại vẫn được quy về cùng một thao tác. */
public record IdempotencyKey(String value) {
    /** Chuẩn hóa khóa và chặn khóa rỗng hoặc vượt quá giới hạn lưu trữ. */
    public IdempotencyKey {
        value = Objects.requireNonNull(value, "idempotency key must not be null").trim();
        if (value.isEmpty()) throw new IllegalArgumentException("idempotency key must not be blank");
        if (value.length() > 100) throw new IllegalArgumentException("idempotency key must not exceed 100 characters");
    }
}
