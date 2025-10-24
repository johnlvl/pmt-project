package com.pmt.backend.service;

import com.pmt.backend.dto.TaskAssignRequest;
import com.pmt.backend.entity.Task;
import com.pmt.backend.entity.TaskAssignment;
import com.pmt.backend.exception.NotProjectMemberException;
import com.pmt.backend.exception.UserNotFoundException;
import com.pmt.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskAssignmentService {
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    public TaskAssignmentService(TaskAssignmentRepository taskAssignmentRepository,
                                 TaskRepository taskRepository,
                                 UserRepository userRepository,
                                 ProjectMemberRepository projectMemberRepository,
                                 NotificationService notificationService,
                                 EmailService emailService) {
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    @Transactional
    public void assign(TaskAssignRequest req) {
        var requester = userRepository.findByEmail(req.getRequesterEmail())
                .orElseThrow(() -> new UserNotFoundException(req.getRequesterEmail()));
        var assignee = userRepository.findByEmail(req.getAssigneeEmail())
                .orElseThrow(() -> new UserNotFoundException(req.getAssigneeEmail()));

        var requesterPmOpt = projectMemberRepository.findByProject_IdAndUser_Email(req.getProjectId(), requester.getEmail());
        if (requesterPmOpt.isEmpty()) throw new NotProjectMemberException(req.getProjectId(), requester.getEmail());
        var requesterRole = requesterPmOpt.get().getRole() != null ? requesterPmOpt.get().getRole().getName() : null;
        if (!"Administrateur".equalsIgnoreCase(requesterRole) && !"Membre".equalsIgnoreCase(requesterRole)) {
            throw new com.pmt.backend.exception.InsufficientProjectPermissionException("Write permission required");
        }

        boolean assigneeIsMember = projectMemberRepository.findByProject_IdAndUser_Email(req.getProjectId(), assignee.getEmail()).isPresent();
        if (!assigneeIsMember) throw new NotProjectMemberException(req.getProjectId(), assignee.getEmail());

        Task task = taskRepository.findById(req.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found: " + req.getTaskId()));

        // Ensure the task belongs to the provided project
        if (task.getProject() == null || task.getProject().getId() == null ||
                !task.getProject().getId().equals(req.getProjectId())) {
            throw new IllegalArgumentException("Task does not belong to project " + req.getProjectId());
        }

        // Clear existing assignment for this task (single assignee design)
        taskAssignmentRepository.deleteByTask_Id(task.getId());

        TaskAssignment ta = new TaskAssignment();
        ta.setTask(task);
        ta.setUser(assignee);
        taskAssignmentRepository.save(ta);

        // Notify assignee (in-app)
        notificationService.notifyUserForTask(assignee.getEmail(), task.getId(),
            "Vous avez été assigné à la tâche '" + task.getName() + "'.");
        // Send email
        emailService.send(assignee.getEmail(),
                "Nouvelle assignation de tâche",
                "Bonjour,\n\nVous avez été assigné à la tâche '" + task.getName() + "' (projet #" + req.getProjectId() + ").\n\nCordialement,\nPMT");
    }
}
