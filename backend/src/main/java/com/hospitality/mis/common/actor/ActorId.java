package com.hospitality.mis.common.actor;

import java.util.Objects;

/** Định danh đã xác thực chịu trách nhiệm cho một thao tác của ứng dụng. */
public record ActorId(String value) {
    /** Giá trị định danh dành cho các tác vụ hệ thống không có người dùng trực tiếp. */
    public static final String SYSTEM_VALUE = "SYSTEM";

    /** Chuẩn hóa và kiểm tra định danh trước khi cho phép dùng trong audit hoặc nghiệp vụ. */
    public ActorId {
        value = Objects.requireNonNull(value, "actor id must not be null").trim();
        if (value.isEmpty()) throw new IllegalArgumentException("actor id must not be blank");
        if (value.length() > 50) throw new IllegalArgumentException("actor id must not exceed 50 characters");
    }

    /** Tạo định danh cố định cho tác nhân hệ thống. */
    public static ActorId system() { return new ActorId(SYSTEM_VALUE); }
}
