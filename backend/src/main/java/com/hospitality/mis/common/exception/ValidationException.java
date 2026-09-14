package com.hospitality.mis.common.exception;

/** Lỗi dữ liệu không đáp ứng điều kiện đầu vào do nghiệp vụ kiểm tra. */
public class ValidationException extends RuntimeException {
    /** Tạo lỗi với thông điệp chỉ ra điều kiện đã bị vi phạm. */
    public ValidationException(String message) {
        super(message);
    }
}
