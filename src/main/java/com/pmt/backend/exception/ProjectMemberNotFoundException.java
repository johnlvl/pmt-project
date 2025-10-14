package com.pmt.backend.exception;

public class ProjectMemberNotFoundException extends RuntimeException {
    public ProjectMemberNotFoundException(Integer projectId, String email) {
        super("Project member not found for project=" + projectId + ", email=" + email);
    }
}
