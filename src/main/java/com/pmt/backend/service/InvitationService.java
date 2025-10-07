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

@Service
public class InvitationService {
    private final ProjectRepository projectRepository;
    private final ProjectInvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public InvitationService(ProjectRepository projectRepository,
                             ProjectInvitationRepository invitationRepository,
                             UserRepository userRepository,
                             RoleRepository roleRepository,
                             ProjectMemberRepository projectMemberRepository) {
        this.projectRepository = projectRepository;
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Transactional
    public Integer sendInvitation(InvitationSendRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + request.getProjectId()));

        ProjectInvitation inv = new ProjectInvitation();
        inv.setProject(project);
        inv.setEmail(request.getEmail());
        invitationRepository.save(inv);
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
}
