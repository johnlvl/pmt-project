package com.pmt.backend.exception;

public class NotProjectMemberException extends RuntimeException {
    public NotProjectMemberException(Integer projectId, String email) {
        super("User " + email + " is not a member of project " + projectId);
    }
}
