package com.pmt.backend.controller;

import com.pmt.backend.dto.ProjectMemberResponse;
import com.pmt.backend.service.ProjectMemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMemberController {
    private final ProjectMemberService projectMemberService;

    public ProjectMemberController(ProjectMemberService projectMemberService) {
        this.projectMemberService = projectMemberService;
    }

    @GetMapping
    public ResponseEntity<List<ProjectMemberResponse>> list(@PathVariable Integer projectId) {
        return ResponseEntity.ok(projectMemberService.listMembers(projectId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> remove(@PathVariable Integer projectId, @PathVariable Integer userId) {
        projectMemberService.removeMember(projectId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
