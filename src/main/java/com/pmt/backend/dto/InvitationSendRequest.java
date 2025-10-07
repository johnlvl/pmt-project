package com.pmt.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public class InvitationSendRequest {
    @NotNull
    private Integer projectId;

    @Email
    @NotNull
    private String email;

    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
