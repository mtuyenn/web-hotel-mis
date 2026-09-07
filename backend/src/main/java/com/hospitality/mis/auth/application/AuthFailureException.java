package com.hospitality.mis.auth.application;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthFailureException extends RuntimeException {
    public AuthFailureException() {
        super("Thông tin xác thực không hợp lệ");
    }
}
