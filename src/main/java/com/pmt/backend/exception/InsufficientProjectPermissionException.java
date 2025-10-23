package com.pmt.backend.exception;

public class InsufficientProjectPermissionException extends RuntimeException {
    public InsufficientProjectPermissionException(String message) {
        super(message);
    }
}
