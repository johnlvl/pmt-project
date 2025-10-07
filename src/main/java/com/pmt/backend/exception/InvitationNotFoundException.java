package com.pmt.backend.exception;

public class InvitationNotFoundException extends RuntimeException {
    public InvitationNotFoundException(Integer id) {
        super("Invitation not found: " + id);
    }
}
