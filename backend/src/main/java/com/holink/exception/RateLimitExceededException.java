package com.holink.exception;

import org.springframework.http.HttpStatus;

public class RateLimitExceededException extends RuntimeException {

    private final HttpStatus status;

    public RateLimitExceededException(String message) {
        super(message);
        this.status = HttpStatus.TOO_MANY_REQUESTS;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
