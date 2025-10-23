package com.pmt.backend.service;

import com.pmt.backend.dto.AssignRoleRequest;
import com.pmt.backend.entity.ProjectMember;
import com.pmt.backend.entity.Role;
import com.pmt.backend.exception.ProjectMemberNotFoundException;
import com.pmt.backend.exception.RoleNotFoundException;
import com.pmt.backend.repository.ProjectMemberRepository;
import com.pmt.backend.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectRoleService {
    private final ProjectMemberRepository projectMemberRepository;
    private final RoleRepository roleRepository;

    public ProjectRoleService(ProjectMemberRepository projectMemberRepository, RoleRepository roleRepository) {
        this.projectMemberRepository = projectMemberRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public void assignRole(AssignRoleRequest request) {
        // Enforce admin for requester when provided
        if (request.getRequesterEmail() != null && !request.getRequesterEmail().isBlank()) {
            var pmOpt = projectMemberRepository.findByProject_IdAndUser_Email(request.getProjectId(), request.getRequesterEmail());
            if (pmOpt.isEmpty()) {
                throw new com.pmt.backend.exception.NotProjectMemberException(request.getProjectId(), request.getRequesterEmail());
            }
            var roleName = pmOpt.get().getRole() != null ? pmOpt.get().getRole().getName() : null;
            if (!"Administrateur".equalsIgnoreCase(roleName)) {
                throw new com.pmt.backend.exception.InsufficientProjectPermissionException("Admin role required");
            }
        }
        ProjectMember member = projectMemberRepository
                .findByProject_IdAndUser_Email(request.getProjectId(), request.getTargetEmail())
                .orElseThrow(() -> new ProjectMemberNotFoundException(request.getProjectId(), request.getTargetEmail()));

        String canonical = canonicalRoleName(request.getRoleName());
        Role role = roleRepository.findByName(canonical)
                .orElseThrow(() -> new RoleNotFoundException(canonical));

        member.setRole(role);
        projectMemberRepository.save(member);
    }

    private String canonicalRoleName(String input) {
        if (input == null) return null;
        String n = input.trim();
        String u = n.toUpperCase();
        switch (u) {
            case "OWNER":
            case "ADMIN":
            case "ADMINISTRATEUR":
                return "Administrateur";
            case "MAINTAINER":
            case "MAINTENEUR":
                return "Mainteneur";
            case "MEMBER":
            case "MEMBRE":
                return "Membre";
            case "OBSERVER":
            case "OBSERVATEUR":
                return "Observateur";
            default:
                return n; // use as-is
        }
    }
}
