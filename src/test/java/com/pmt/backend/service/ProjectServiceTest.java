package com.pmt.backend.service;

import com.pmt.backend.dto.ProjectCreateRequest;
import com.pmt.backend.dto.ProjectResponse;
import com.pmt.backend.entity.Project;
import com.pmt.backend.entity.ProjectMember;
import com.pmt.backend.entity.Role;
import com.pmt.backend.entity.User;
import com.pmt.backend.exception.RoleNotFoundException;
import com.pmt.backend.exception.UserNotFoundException;
import com.pmt.backend.repository.ProjectMemberRepository;
import com.pmt.backend.repository.ProjectRepository;
import com.pmt.backend.repository.RoleRepository;
import com.pmt.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock ProjectRepository projectRepository;
    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock ProjectMemberRepository projectMemberRepository;

    @InjectMocks ProjectService projectService;

    @Test
    void create_shouldCreateProject_andAssignAdmin() {
        ProjectCreateRequest req = new ProjectCreateRequest();
        req.setName("Projet Alpha");
        req.setDescription("Desc");
        req.setStartDate("2025-09-01");
        req.setCreatorEmail("alice@example.com");

        User creator = new User();
        creator.setId(1);
        creator.setEmail("alice@example.com");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(creator));

        Role admin = new Role();
        admin.setId(1);
        admin.setName("Administrateur");
        when(roleRepository.findByName("Administrateur")).thenReturn(Optional.of(admin));

        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> {
            Project p = inv.getArgument(0);
            p.setId(100);
            return p;
        });

        ProjectResponse res = projectService.create(req);

        assertEquals(100, res.getId());
        assertEquals("Projet Alpha", res.getName());
        assertEquals("2025-09-01", res.getStartDate());

        ArgumentCaptor<ProjectMember> pmCaptor = ArgumentCaptor.forClass(ProjectMember.class);
        verify(projectMemberRepository).save(pmCaptor.capture());
        assertEquals(creator, pmCaptor.getValue().getUser());
        assertEquals(admin, pmCaptor.getValue().getRole());
    }

    @Test
    void create_shouldFail_whenUserNotFound() {
        ProjectCreateRequest req = new ProjectCreateRequest();
        req.setName("Projet");
        req.setCreatorEmail("nobody@example.com");

        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> projectService.create(req));
        verify(projectRepository, never()).save(any());
    }

    @Test
    void create_shouldFail_whenAdminRoleMissing() {
        ProjectCreateRequest req = new ProjectCreateRequest();
        req.setName("Projet");
        req.setCreatorEmail("alice@example.com");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(new User()));
        when(roleRepository.findByName("Administrateur")).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () -> projectService.create(req));
        verify(projectRepository, never()).save(any());
    }
}
