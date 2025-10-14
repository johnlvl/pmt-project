package com.pmt.backend.dto;

import java.util.List;
import java.util.Map;

public class TaskBoardResponse {
    private Map<String, List<TaskListItem>> lanes;

    public TaskBoardResponse() {}
    public TaskBoardResponse(Map<String, List<TaskListItem>> lanes) { this.lanes = lanes; }

    public Map<String, List<TaskListItem>> getLanes() { return lanes; }
    public void setLanes(Map<String, List<TaskListItem>> lanes) { this.lanes = lanes; }
}
