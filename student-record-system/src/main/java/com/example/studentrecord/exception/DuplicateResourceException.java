package com.example.studentrecord.exception;

/** Thrown when a unique field (email, roll number or course code) is reused. Mapped to HTTP 409. */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
