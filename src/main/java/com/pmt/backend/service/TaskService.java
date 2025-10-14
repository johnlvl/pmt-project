package com.pmt.backend.service;

import com.pmt.backend.dto.TaskCreateRequest;
import com.pmt.backend.dto.TaskResponse;
import com.pmt.backend.entity.Project;
import com.pmt.backend.entity.Task;
import com.pmt.backend.exception.NotProjectMemberException;
import com.pmt.backend.exception.UserNotFoundException;
import com.pmt.backend.repository.ProjectMemberRepository;
import com.pmt.backend.repository.ProjectRepository;
import com.pmt.backend.repository.TaskRepository;
import com.pmt.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       UserRepository userRepository,
                       ProjectMemberRepository projectMemberRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Transactional
    public TaskResponse create(TaskCreateRequest req) {
        // requester must exist
        var requester = userRepository.findByEmail(req.getRequesterEmail())
                .orElseThrow(() -> new UserNotFoundException(req.getRequesterEmail()));

        // requester must be a member of the project
        boolean isMember = projectMemberRepository
                .findByProject_IdAndUser_Email(req.getProjectId(), requester.getEmail())
                .isPresent();
        if (!isMember) throw new NotProjectMemberException(req.getProjectId(), requester.getEmail());

        Project project = projectRepository.findById(req.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found: " + req.getProjectId()));

        Task task = new Task();
        task.setProject(project);
        task.setName(req.getName());
        task.setDescription(req.getDescription());
        task.setPriority(req.getPriority());
        task.setStatus("TODO");
        if (req.getDueDate() != null && !req.getDueDate().isBlank()) {
            task.setDueDate(parseDate(req.getDueDate()));
        }

        Task saved = taskRepository.save(task);
        return toResponse(saved);
    }

    private static Date parseDate(String s) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(s);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format (yyyy-MM-dd expected): " + s);
        }
    }

    private static TaskResponse toResponse(Task t) {
        String due = t.getDueDate() != null ? new SimpleDateFormat("yyyy-MM-dd").format(t.getDueDate()) : null;
        String end = t.getEndDate() != null ? new SimpleDateFormat("yyyy-MM-dd").format(t.getEndDate()) : null;
        return new TaskResponse(
                t.getId(),
                t.getProject() != null ? t.getProject().getId() : null,
                t.getName(),
                t.getDescription(),
                due,
                end,
                t.getPriority(),
                t.getStatus()
        );
    }
}
