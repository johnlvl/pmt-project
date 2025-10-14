package com.pmt.backend.controller;

import com.pmt.backend.dto.TaskCreateRequest;
import com.pmt.backend.dto.TaskResponse;
import com.pmt.backend.dto.TaskUpdateRequest;
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

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(NotProjectMemberException.class)
    public ResponseEntity<String> handleNotMember(NotProjectMemberException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }
}
