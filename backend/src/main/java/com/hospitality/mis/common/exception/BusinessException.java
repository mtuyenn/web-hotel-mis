package com.hospitality.mis.common.exception;

/** Lỗi nghiệp vụ tổng quát khi thao tác không thể thực hiện theo quy tắc hiện hành. */
public class BusinessException extends RuntimeException {
    /** Tạo lỗi với thông điệp giải thích nguyên nhân cho lớp xử lý phía trên. */
    public BusinessException(String message) {
        super(message);
    }
}
