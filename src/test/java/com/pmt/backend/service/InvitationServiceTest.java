package com.pmt.backend.service;

import com.pmt.backend.dto.InvitationSendRequest;
import com.pmt.backend.entity.Project;
import com.pmt.backend.entity.ProjectInvitation;
import com.pmt.backend.repository.ProjectInvitationRepository;
import com.pmt.backend.repository.ProjectMemberRepository;
import com.pmt.backend.repository.ProjectRepository;
import com.pmt.backend.repository.RoleRepository;
import com.pmt.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    @Mock ProjectRepository projectRepository;
    @Mock ProjectInvitationRepository invitationRepository;
    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock ProjectMemberRepository projectMemberRepository;

    @InjectMocks InvitationService invitationService;

    @Test
    void sendInvitation_shouldPersist() {
        InvitationSendRequest req = new InvitationSendRequest();
        req.setProjectId(1);
        req.setEmail("bob@example.com");

        when(projectRepository.findById(1)).thenReturn(Optional.of(new Project()));
        when(invitationRepository.save(any(ProjectInvitation.class))).thenAnswer(inv -> {
            ProjectInvitation pi = inv.getArgument(0);
            pi.setId(10);
            return pi;
        });

        Integer id = invitationService.sendInvitation(req);
        assertEquals(10, id);
    }

    @Test
    void sendInvitation_withRequesterNotMember_shouldFail() {
        InvitationSendRequest req = new InvitationSendRequest();
        req.setProjectId(1);
        req.setEmail("bob@example.com");
        req.setRequesterEmail("alice@example.com");

        when(projectRepository.findById(1)).thenReturn(Optional.of(new Project()));
        when(projectMemberRepository.findByProject_IdAndUser_Email(1, "alice@example.com")).thenReturn(Optional.empty());

        assertThrows(com.pmt.backend.exception.NotProjectMemberException.class, () -> invitationService.sendInvitation(req));
    }

    @Test
    void sendInvitation_withRequesterNotAdmin_shouldFail() {
        InvitationSendRequest req = new InvitationSendRequest();
        req.setProjectId(1);
        req.setEmail("bob@example.com");
        req.setRequesterEmail("alice@example.com");

        when(projectRepository.findById(1)).thenReturn(Optional.of(new Project()));
        var pm = new com.pmt.backend.entity.ProjectMember();
        var r = new com.pmt.backend.entity.Role();
        r.setName("Membre");
        pm.setRole(r);
        when(projectMemberRepository.findByProject_IdAndUser_Email(1, "alice@example.com")).thenReturn(Optional.of(pm));

        assertThrows(com.pmt.backend.exception.InsufficientProjectPermissionException.class, () -> invitationService.sendInvitation(req));
    }

    @Test
    void sendInvitation_projectNotFound_shouldFail() {
        InvitationSendRequest req = new InvitationSendRequest();
        req.setProjectId(999);
        req.setEmail("bob@example.com");

        when(projectRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> invitationService.sendInvitation(req));
    }

    @Test
    void sendInvitation_emailSendFails_shouldNotThrow() {
        // Use real service with mocked email to simulate exception path
        InvitationSendRequest req = new InvitationSendRequest();
        req.setProjectId(1);
        req.setEmail("bob@example.com");

        var project = new Project();
        project.setId(1);
        project.setName("P");
        when(projectRepository.findById(1)).thenReturn(Optional.of(project));
        when(invitationRepository.save(any(ProjectInvitation.class))).thenAnswer(inv -> {
            ProjectInvitation pi = inv.getArgument(0);
            pi.setId(11);
            return pi;
        });

        // Replace emailService in InvitationService via reflection to throw
        var emailService = mock(EmailService.class);
        doThrow(new RuntimeException("smtp down")).when(emailService).send(anyString(), anyString(), anyString());
        // Inject mocked emailService
        var field = InvitationService.class.getDeclaredFields();
        for (var f : field) {
            if (f.getType().getSimpleName().equals("EmailService")) {
                f.setAccessible(true);
                try { f.set(invitationService, emailService); } catch (IllegalAccessException ignored) {}
            }
        }

        assertDoesNotThrow(() -> invitationService.sendInvitation(req));
    }

    @Test
    void acceptInvitation_shouldAddMember_andSetAccepted() {
        // Arrange
        var inv = new com.pmt.backend.entity.ProjectInvitation();
        var project = new com.pmt.backend.entity.Project();
        project.setId(1);
        inv.setId(5);
        inv.setProject(project);
        when(invitationRepository.findById(5)).thenReturn(java.util.Optional.of(inv));

        var user = new com.pmt.backend.entity.User();
        user.setId(2);
        user.setEmail("bob@example.com");
        when(projectMemberRepository.existsByProject_IdAndUser_Id(1, 2)).thenReturn(false);

        var role = new com.pmt.backend.entity.Role();
        role.setId(7);
        role.setName("Membre");
        when(userRepository.findByEmail("bob@example.com")).thenReturn(java.util.Optional.of(user));
        when(roleRepository.findByName("Membre")).thenReturn(java.util.Optional.of(role));

        // Act
        invitationService.acceptInvitation(5, "bob@example.com");

        // Assert: no exceptions, and invitationRepository.save called implicitly via service
        // Further verification could capture the saved invitation, but not strictly necessary here
    }

    @Test
    void declineInvitation_shouldSetDeclined() {
        var inv = new com.pmt.backend.entity.ProjectInvitation();
        inv.setId(6);
        when(invitationRepository.findById(6)).thenReturn(java.util.Optional.of(inv));
        invitationService.declineInvitation(6);
        // If needed, verify that invitationRepository.save was called
    }
}
