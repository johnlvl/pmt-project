package com.pmt.backend.controller;

import com.pmt.backend.dto.TaskCreateRequest;
import com.pmt.backend.dto.TaskResponse;
import com.pmt.backend.dto.TaskUpdateRequest;
import com.pmt.backend.dto.TaskListItem;
import com.pmt.backend.dto.TaskBoardResponse;
import com.pmt.backend.entity.TaskHistory;
import com.pmt.backend.exception.NotProjectMemberException;
import com.pmt.backend.exception.UserNotFoundException;
import com.pmt.backend.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskCreateRequest req) {
        TaskResponse resp = taskService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PatchMapping("/update")
    public ResponseEntity<TaskResponse> update(@Valid @RequestBody TaskUpdateRequest req) {
        TaskResponse resp = taskService.update(req);
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    public ResponseEntity<java.util.List<TaskListItem>> list(
            @RequestParam Integer projectId,
            @RequestParam String requesterEmail,
            @RequestParam(required = false) String assigneeEmail) {
        return ResponseEntity.ok(taskService.list(projectId, requesterEmail, assigneeEmail));
    }

    @GetMapping("/board")
    public ResponseEntity<TaskBoardResponse> board(
            @RequestParam Integer projectId,
            @RequestParam String requesterEmail) {
        return ResponseEntity.ok(taskService.board(projectId, requesterEmail));
    }

    @GetMapping("/{taskId}/history")
    public ResponseEntity<java.util.List<TaskHistory>> history(
            @PathVariable Integer taskId,
            @RequestParam Integer projectId,
            @RequestParam String requesterEmail) {
        return ResponseEntity.ok(taskService.history(taskId, projectId, requesterEmail));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(NotProjectMemberException.class)
    public ResponseEntity<String> handleNotMember(NotProjectMemberException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }
}
