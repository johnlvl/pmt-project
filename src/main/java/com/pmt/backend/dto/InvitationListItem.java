package com.pmt.backend.dto;

import com.pmt.backend.entity.InvitationStatus;
import java.util.Date;

public class InvitationListItem {
    private Integer id;
    private Integer projectId;
    private String projectName;
    private String email;
    private InvitationStatus status;
    private Date createdAt;

    public InvitationListItem() {}

    public InvitationListItem(Integer id, Integer projectId, String projectName, String email, InvitationStatus status, Date createdAt) {
        this.id = id;
        this.projectId = projectId;
        this.projectName = projectName;
        this.email = email;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public InvitationStatus getStatus() { return status; }
    public void setStatus(InvitationStatus status) { this.status = status; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
