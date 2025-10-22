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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import com.pmt.backend.exception.ProjectNotFoundException;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public ProjectService(ProjectRepository projectRepository,
                          UserRepository userRepository,
                          RoleRepository roleRepository,
                          ProjectMemberRepository projectMemberRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Transactional
    public ProjectResponse create(ProjectCreateRequest request) {
        User creator = userRepository.findByEmail(request.getCreatorEmail())
                .orElseThrow(() -> new UserNotFoundException(request.getCreatorEmail()));

        Role adminRole = roleRepository.findByName("Administrateur")
                .orElseThrow(() -> new RoleNotFoundException("Administrateur"));

        Project p = new Project();
        p.setName(request.getName());
        p.setDescription(request.getDescription());
        if (request.getStartDate() != null && !request.getStartDate().isBlank()) {
            p.setStartDate(parseDate(request.getStartDate()));
        }
        Project saved = projectRepository.save(p);

        ProjectMember pm = new ProjectMember();
        pm.setProject(saved);
        pm.setUser(creator);
        pm.setRole(adminRole);
        projectMemberRepository.save(pm);

        return toResponse(saved);
    }

    private Date parseDate(String s) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(s);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format, expected yyyy-MM-dd");
        }
    }

    public Page<ProjectResponse> list(Pageable pageable) {
        return projectRepository.findAll(pageable).map(this::toResponse);
    }

    public ProjectResponse getById(Integer projectId) {
        return projectRepository.findById(projectId)
                .map(this::toResponse)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
    }

    private ProjectResponse toResponse(Project p) {
        String isoDate = p.getStartDate() != null ? new SimpleDateFormat("yyyy-MM-dd").format(p.getStartDate()) : null;
        return new ProjectResponse(p.getId(), p.getName(), p.getDescription(), isoDate);
    }
}
