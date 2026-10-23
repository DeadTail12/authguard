package com.authguard.exception;

import org.springframework.http.HttpStatus;

import java.time.Instant;

/** One shape for every error body the API returns, so clients don't have to special-case each endpoint. */
public record ErrorResponse(Instant timestamp, int status, String error, String message) {
    public static ErrorResponse of(HttpStatus status, String message) {
        return new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message);
    }
}
