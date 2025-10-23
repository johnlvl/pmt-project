package com.pmt.backend.dto;

public class TaskListItem {
    public Integer id;
    public String name;
    public String description;
    public String status;
    public String priority;
    public String dueDate;
    public String assigneeEmail;

    public TaskListItem() {}
    public TaskListItem(Integer id, String name, String status, String priority, String dueDate) {
        this.id = id; this.name = name; this.status = status; this.priority = priority; this.dueDate = dueDate;
    }
}
