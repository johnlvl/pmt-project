package com.pmt.backend.service;

import com.pmt.backend.dto.AssignRoleRequest;
import com.pmt.backend.entity.ProjectMember;
import com.pmt.backend.entity.Role;
import com.pmt.backend.exception.ProjectMemberNotFoundException;
import com.pmt.backend.exception.RoleNotFoundException;
import com.pmt.backend.repository.ProjectMemberRepository;
import com.pmt.backend.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectRoleServiceTest {

    @Mock ProjectMemberRepository projectMemberRepository;
    @Mock RoleRepository roleRepository;

    @InjectMocks ProjectRoleService projectRoleService;

    @Test
    void assignRole_shouldUpdateRole() {
        AssignRoleRequest req = new AssignRoleRequest();
        req.setProjectId(1);
        req.setTargetEmail("bob@example.com");
        req.setRoleName("Membre");

        ProjectMember pm = new ProjectMember();
        when(projectMemberRepository.findByProject_IdAndUser_Email(1, "bob@example.com")).thenReturn(Optional.of(pm));

        Role role = new Role();
        role.setName("Membre");
        when(roleRepository.findByName("Membre")).thenReturn(Optional.of(role));

        projectRoleService.assignRole(req);

        verify(projectMemberRepository).save(pm);
        assertEquals("Membre", pm.getRole().getName());
    }

    @Test
    void assignRole_shouldFail_whenMemberMissing() {
        AssignRoleRequest req = new AssignRoleRequest();
        req.setProjectId(1);
        req.setTargetEmail("none@example.com");
        req.setRoleName("Membre");

        when(projectMemberRepository.findByProject_IdAndUser_Email(1, "none@example.com")).thenReturn(Optional.empty());

        assertThrows(ProjectMemberNotFoundException.class, () -> projectRoleService.assignRole(req));
        verify(projectMemberRepository, never()).save(any());
    }

    @Test
    void assignRole_shouldFail_whenRoleMissing() {
        AssignRoleRequest req = new AssignRoleRequest();
        req.setProjectId(1);
        req.setTargetEmail("bob@example.com");
        req.setRoleName("Inconnu");

        when(projectMemberRepository.findByProject_IdAndUser_Email(1, "bob@example.com")).thenReturn(Optional.of(new ProjectMember()));
        when(roleRepository.findByName("Inconnu")).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () -> projectRoleService.assignRole(req));
        verify(projectMemberRepository, never()).save(any());
    }

    @Test
    void assignRole_shouldFail_whenRequesterNotMember() {
        AssignRoleRequest req = new AssignRoleRequest();
        req.setProjectId(1);
        req.setRequesterEmail("alice@example.com");
        req.setTargetEmail("bob@example.com");
        req.setRoleName("Membre");

        when(projectMemberRepository.findByProject_IdAndUser_Email(1, "alice@example.com")).thenReturn(Optional.empty());

        assertThrows(com.pmt.backend.exception.NotProjectMemberException.class, () -> projectRoleService.assignRole(req));
        verify(projectMemberRepository, never()).save(any());
    }

    @Test
    void assignRole_shouldFail_whenRequesterNotAdmin() {
        AssignRoleRequest req = new AssignRoleRequest();
        req.setProjectId(1);
        req.setRequesterEmail("alice@example.com");
        req.setTargetEmail("bob@example.com");
        req.setRoleName("Membre");

        var pm = new ProjectMember();
        var role = new Role();
        role.setName("Membre"); // not admin
        pm.setRole(role);
        when(projectMemberRepository.findByProject_IdAndUser_Email(1, "alice@example.com")).thenReturn(Optional.of(pm));

        assertThrows(com.pmt.backend.exception.InsufficientProjectPermissionException.class, () -> projectRoleService.assignRole(req));
        verify(projectMemberRepository, never()).save(any());
    }
}
