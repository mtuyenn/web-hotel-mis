package com.hospitality.mis.middleware;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hospitality.mis.service.auth.AuthFailureException;
import com.hospitality.mis.common.exception.DomainException;
import com.hospitality.mis.controller.auth.AuthController;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice(basePackageClasses = AuthController.class)
@Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
public class AuthErrorHandler {
    @ExceptionHandler(AuthFailureException.class)
    ResponseEntity<ErrorResponse> authenticationFailure(AuthFailureException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(exception.getCode()));
    }

    @ExceptionHandler(DomainException.class)
    ResponseEntity<ErrorResponse> domainFailure(DomainException exception) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse(exception.getCode()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class,
            ConstraintViolationException.class})
    ResponseEntity<ErrorResponse> invalidRequest(Exception exception) {
        return ResponseEntity.badRequest().body(new ErrorResponse("INVALID_REQUEST"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorResponse> accessDenied() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse("ACCESS_DENIED"));
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    record ErrorResponse(String error) {
    }
}
