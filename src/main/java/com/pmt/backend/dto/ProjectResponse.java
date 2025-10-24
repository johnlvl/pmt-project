package com.pmt.backend.dto;

public class ProjectResponse {
    private Integer id;
    private String name;
    private String description;
    private String startDate; // ISO string

    public ProjectResponse() {}

    public ProjectResponse(Integer id, String name, String description, String startDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
}
