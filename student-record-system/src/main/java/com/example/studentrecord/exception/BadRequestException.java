package com.example.studentrecord.exception;

/** Thrown when a request is syntactically valid but breaks a business rule. */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
