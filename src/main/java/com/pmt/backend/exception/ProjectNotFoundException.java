package com.pmt.backend.exception;

public class ProjectNotFoundException extends RuntimeException {
    public ProjectNotFoundException(Integer projectId) {
        super("Project not found: " + projectId);
    }
}
