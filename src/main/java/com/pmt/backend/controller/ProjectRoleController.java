package com.pmt.backend.controller;

import com.pmt.backend.dto.AssignRoleRequest;
import com.pmt.backend.exception.ProjectMemberNotFoundException;
import com.pmt.backend.exception.RoleNotFoundException;
import com.pmt.backend.exception.InsufficientProjectPermissionException;
import com.pmt.backend.service.ProjectRoleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectRoleController {
    private final ProjectRoleService projectRoleService;

    public ProjectRoleController(ProjectRoleService projectRoleService) {
        this.projectRoleService = projectRoleService;
    }

    @PostMapping("/assign-role")
    public ResponseEntity<Void> assignRole(@Valid @RequestBody AssignRoleRequest request) {
        projectRoleService.assignRole(request);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ProjectMemberNotFoundException.class)
    public ResponseEntity<String> handleMemberNotFound(ProjectMemberNotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<String> handleRoleNotFound(RoleNotFoundException ex) {
        return ResponseEntity.status(422).body(ex.getMessage());
    }

    @ExceptionHandler(InsufficientProjectPermissionException.class)
    public ResponseEntity<String> handleForbidden(InsufficientProjectPermissionException ex) {
        return ResponseEntity.status(403).body(ex.getMessage());
    }
}
