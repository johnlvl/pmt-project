package com.pmt.backend.service;

import com.pmt.backend.dto.InvitationSendRequest;
import com.pmt.backend.entity.*;
import com.pmt.backend.exception.InvitationNotFoundException;
import com.pmt.backend.exception.RoleNotFoundException;
import com.pmt.backend.repository.ProjectInvitationRepository;
import com.pmt.backend.repository.ProjectMemberRepository;
import com.pmt.backend.repository.ProjectRepository;
import com.pmt.backend.repository.RoleRepository;
import com.pmt.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import com.pmt.backend.entity.InvitationStatus;

@Service
public class InvitationService {
    private final ProjectRepository projectRepository;
    private final ProjectInvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final EmailService emailService;

    public InvitationService(ProjectRepository projectRepository,
                             ProjectInvitationRepository invitationRepository,
                             UserRepository userRepository,
                             RoleRepository roleRepository,
                             ProjectMemberRepository projectMemberRepository,
                             EmailService emailService) {
        this.projectRepository = projectRepository;
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.emailService = emailService;
    }

    @Transactional
    public Integer sendInvitation(InvitationSendRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + request.getProjectId()));

        ProjectInvitation inv = new ProjectInvitation();
        inv.setProject(project);
        inv.setEmail(request.getEmail());
        invitationRepository.save(inv);

        // Send email notification (mocked via NoopEmailService)
        try {
            String subject = "Invitation à rejoindre le projet: " + project.getName();
            String body = "Vous avez été invité à rejoindre le projet '" + project.getName() + "'.\n" +
                    "Connectez-vous puis acceptez l'invitation (flux d'acceptation simplifié dans cette version).";
            emailService.send(request.getEmail(), subject, body);
        } catch (Exception ignored) {
            // Best effort: ne bloque pas l'invitation si l'envoi d'email échoue
        }
        return inv.getId();
    }

    @Transactional
    public void acceptInvitation(Integer invitationId, String userEmail) {
        ProjectInvitation inv = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new InvitationNotFoundException(invitationId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        // Assign default role 'Membre'
        Role memberRole = roleRepository.findByName("Membre")
                .orElseThrow(() -> new RoleNotFoundException("Membre"));

        if (!projectMemberRepository.existsByProject_IdAndUser_Id(inv.getProject().getId(), user.getId())) {
            ProjectMember pm = new ProjectMember();
            pm.setProject(inv.getProject());
            pm.setUser(user);
            pm.setRole(memberRole);
            projectMemberRepository.save(pm);
        }

        inv.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(inv);
    }

    @Transactional
    public void declineInvitation(Integer invitationId) {
        ProjectInvitation inv = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new InvitationNotFoundException(invitationId));
        inv.setStatus(InvitationStatus.DECLINED);
        invitationRepository.save(inv);
    }

    @Transactional(readOnly = true)
    public List<ProjectInvitation> getInvitationsFor(String email, InvitationStatus status) {
        return invitationRepository.findByEmailAndStatusOrderByCreatedAtDesc(email, status);
    }
}
