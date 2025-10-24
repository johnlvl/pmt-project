package com.pmt.backend.service;

import com.pmt.backend.dto.ProjectMemberResponse;
import com.pmt.backend.entity.ProjectMember;
import com.pmt.backend.repository.ProjectMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectMemberService {
    private final ProjectMemberRepository projectMemberRepository;

    public ProjectMemberService(ProjectMemberRepository projectMemberRepository) {
        this.projectMemberRepository = projectMemberRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> listMembers(Integer projectId) {
        return projectMemberRepository.findByProject_Id(projectId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeMember(Integer projectId, Integer userId) {
        ProjectMember pm = projectMemberRepository.findByProject_IdAndUser_Id(projectId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Project member not found for project=" + projectId + ", userId=" + userId));
        projectMemberRepository.delete(pm);
    }

    private ProjectMemberResponse toResponse(ProjectMember pm) {
        return new ProjectMemberResponse(
                pm.getUser().getId(),
                pm.getUser().getUsername(),
                pm.getUser().getEmail(),
                pm.getRole() != null ? pm.getRole().getName() : null
        );
    }
}
