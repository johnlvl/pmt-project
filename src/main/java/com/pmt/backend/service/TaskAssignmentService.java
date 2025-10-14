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

    public TaskAssignmentService(TaskAssignmentRepository taskAssignmentRepository,
                                 TaskRepository taskRepository,
                                 UserRepository userRepository,
                                 ProjectMemberRepository projectMemberRepository) {
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Transactional
    public void assign(TaskAssignRequest req) {
        var requester = userRepository.findByEmail(req.getRequesterEmail())
                .orElseThrow(() -> new UserNotFoundException(req.getRequesterEmail()));
        var assignee = userRepository.findByEmail(req.getAssigneeEmail())
                .orElseThrow(() -> new UserNotFoundException(req.getAssigneeEmail()));

        boolean requesterIsMember = projectMemberRepository.findByProject_IdAndUser_Email(req.getProjectId(), requester.getEmail()).isPresent();
        if (!requesterIsMember) throw new NotProjectMemberException(req.getProjectId(), requester.getEmail());

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
    }
}
