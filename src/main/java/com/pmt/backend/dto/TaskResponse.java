package com.pmt.backend.dto;

public class TaskResponse {
    private Integer id;
    private Integer projectId;
    private String name;
    private String description;
    private String dueDate;
    private String endDate;
    private String priority;
    private String status;

    public TaskResponse() {}

    public TaskResponse(Integer id, Integer projectId, String name, String description,
                        String dueDate, String endDate, String priority, String status) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
        this.endDate = endDate;
        this.priority = priority;
        this.status = status;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
