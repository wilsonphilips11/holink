package com.holink.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends RuntimeException {

    private final HttpStatus status;

    public ConflictException(String message) {
        super(message);
        this.status = HttpStatus.CONFLICT;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
