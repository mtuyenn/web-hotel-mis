package com.hospitality.mis.common.exception;



import com.hospitality.mis.common.api.ApiError;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;

import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.web.bind.annotation.RestControllerAdvice;



import java.time.Instant;

import java.util.List;



@RestControllerAdvice

public class GlobalExceptionHandler {



    @ExceptionHandler(DomainException.class)

    public ResponseEntity<ApiError> handleDomain(DomainException exception) {

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(

                new ApiError(Instant.now(), 422, exception.getCode(), exception.getMessage(), List.of()));

    }



    @ExceptionHandler(MethodArgumentNotValidException.class)

    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {

        var details = exception.getBindingResult().getFieldErrors().stream()

                .map(error -> error.getField() + ": " + error.getDefaultMessage()).toList();

        return ResponseEntity.badRequest().body(new ApiError(Instant.now(), 400, "VALIDATION_ERROR",

                "Invalid request data", details));

    }



    @ExceptionHandler(DataIntegrityViolationException.class)

    public ResponseEntity<ApiError> handleConstraint(DataIntegrityViolationException exception) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(Instant.now(), 409,

                "DATA_CONFLICT", "Data already exists or was changed by another operation", List.of()));

    }

}
