package com.rofs.spring.security.config.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;


@Getter
public class ErrorResponse {
    private final String timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;


    @Builder(builderMethodName = "of")
    private ErrorResponse(int status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now().toString();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

}