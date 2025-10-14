package com.pmt.backend.service;

import com.pmt.backend.dto.TaskCreateRequest;
import com.pmt.backend.dto.TaskResponse;
import com.pmt.backend.dto.TaskUpdateRequest;
import com.pmt.backend.entity.Project;
import com.pmt.backend.entity.Task;
import com.pmt.backend.entity.TaskHistory;
import com.pmt.backend.exception.NotProjectMemberException;
import com.pmt.backend.exception.UserNotFoundException;
import com.pmt.backend.repository.ProjectMemberRepository;
import com.pmt.backend.repository.ProjectRepository;
import com.pmt.backend.repository.TaskRepository;
import com.pmt.backend.repository.TaskHistoryRepository;
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
    private final TaskHistoryRepository taskHistoryRepository;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       UserRepository userRepository,
                       ProjectMemberRepository projectMemberRepository,
                       TaskHistoryRepository taskHistoryRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskHistoryRepository = taskHistoryRepository;
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

    @Transactional
    public TaskResponse update(TaskUpdateRequest req) {
        var requester = userRepository.findByEmail(req.getRequesterEmail())
                .orElseThrow(() -> new UserNotFoundException(req.getRequesterEmail()));

        boolean isMember = projectMemberRepository
                .findByProject_IdAndUser_Email(req.getProjectId(), requester.getEmail())
                .isPresent();
        if (!isMember) throw new NotProjectMemberException(req.getProjectId(), requester.getEmail());

        Task task = taskRepository.findById(req.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found: " + req.getTaskId()));
        if (task.getProject() == null || task.getProject().getId() == null ||
                !task.getProject().getId().equals(req.getProjectId())) {
            throw new IllegalArgumentException("Task does not belong to project " + req.getProjectId());
        }

        StringBuilder changes = new StringBuilder();
        if (req.getName() != null && !req.getName().equals(task.getName())) {
            changes.append("name: '" + task.getName() + "' -> '" + req.getName() + "'\n");
            task.setName(req.getName());
        }
        if (req.getDescription() != null && !req.getDescription().equals(task.getDescription())) {
            changes.append("description changed\n");
            task.setDescription(req.getDescription());
        }
        if (req.getPriority() != null && !req.getPriority().equals(task.getPriority())) {
            changes.append("priority: '" + task.getPriority() + "' -> '" + req.getPriority() + "'\n");
            task.setPriority(req.getPriority());
        }
        if (req.getStatus() != null && !req.getStatus().equals(task.getStatus())) {
            changes.append("status: '" + task.getStatus() + "' -> '" + req.getStatus() + "'\n");
            task.setStatus(req.getStatus());
        }
        if (req.getDueDate() != null) {
            var newDate = req.getDueDate().isBlank() ? null : parseDate(req.getDueDate());
            var oldStr = task.getDueDate() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(task.getDueDate()) : null;
            var newStr = newDate != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(newDate) : null;
            if (!java.util.Objects.equals(oldStr, newStr)) {
                changes.append("dueDate: '" + oldStr + "' -> '" + newStr + "'\n");
                task.setDueDate(newDate);
            }
        }
        if (req.getEndDate() != null) {
            var newDate = req.getEndDate().isBlank() ? null : parseDate(req.getEndDate());
            var oldStr = task.getEndDate() != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(task.getEndDate()) : null;
            var newStr = newDate != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(newDate) : null;
            if (!java.util.Objects.equals(oldStr, newStr)) {
                changes.append("endDate: '" + oldStr + "' -> '" + newStr + "'\n");
                task.setEndDate(newDate);
            }
        }

        Task saved = taskRepository.save(task);

        if (changes.length() > 0) {
            TaskHistory h = new TaskHistory();
            h.setTask(saved);
            h.setChangedBy(requester);
            h.setChangeDate(new java.util.Date());
            h.setChangeDescription(changes.toString());
            taskHistoryRepository.save(h);
        }

        return toResponse(saved);
    }
}
