package com.pmt.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public class InvitationSendRequest {
    @NotNull
    private Integer projectId;

    @Email
    @NotNull
    private String email;

    // Optional for legacy callers; used for permission checks when provided
    private String requesterEmail;

    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRequesterEmail() { return requesterEmail; }
    public void setRequesterEmail(String requesterEmail) { this.requesterEmail = requesterEmail; }
}
