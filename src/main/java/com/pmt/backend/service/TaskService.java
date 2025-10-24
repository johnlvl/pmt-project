package com.pmt.backend.service;

import com.pmt.backend.dto.TaskCreateRequest;
import com.pmt.backend.dto.TaskResponse;
import com.pmt.backend.dto.TaskUpdateRequest;
import com.pmt.backend.dto.TaskListItem;
import com.pmt.backend.dto.TaskBoardResponse;
import com.pmt.backend.entity.Project;
import com.pmt.backend.entity.Task;
import com.pmt.backend.entity.TaskHistory;
import com.pmt.backend.exception.NotProjectMemberException;
import com.pmt.backend.exception.UserNotFoundException;
import com.pmt.backend.repository.ProjectMemberRepository;
import com.pmt.backend.repository.ProjectRepository;
import com.pmt.backend.repository.TaskRepository;
import com.pmt.backend.repository.TaskHistoryRepository;
import com.pmt.backend.repository.TaskAssignmentRepository;
import com.pmt.backend.repository.UserRepository;
import com.pmt.backend.service.NotificationService;
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
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final NotificationService notificationService;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       UserRepository userRepository,
                       ProjectMemberRepository projectMemberRepository,
                       TaskHistoryRepository taskHistoryRepository,
                       TaskAssignmentRepository taskAssignmentRepository,
                       NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskHistoryRepository = taskHistoryRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public TaskResponse create(TaskCreateRequest req) {
        // requester must exist
        var requester = userRepository.findByEmail(req.getRequesterEmail())
                .orElseThrow(() -> new UserNotFoundException(req.getRequesterEmail()));

    // requester must have write permission (Admin or Membre)
    var pmOpt = projectMemberRepository.findByProject_IdAndUser_Email(req.getProjectId(), requester.getEmail());
    if (pmOpt.isEmpty()) throw new NotProjectMemberException(req.getProjectId(), requester.getEmail());
    var roleName = pmOpt.get().getRole() != null ? pmOpt.get().getRole().getName() : null;
    if (!"Administrateur".equalsIgnoreCase(roleName) && !"Membre".equalsIgnoreCase(roleName)) {
        throw new com.pmt.backend.exception.InsufficientProjectPermissionException("Write permission required");
    }

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

    var pmOpt = projectMemberRepository.findByProject_IdAndUser_Email(req.getProjectId(), requester.getEmail());
    if (pmOpt.isEmpty()) throw new NotProjectMemberException(req.getProjectId(), requester.getEmail());
    var roleName = pmOpt.get().getRole() != null ? pmOpt.get().getRole().getName() : null;
    if (!"Administrateur".equalsIgnoreCase(roleName) && !"Membre".equalsIgnoreCase(roleName)) {
        throw new com.pmt.backend.exception.InsufficientProjectPermissionException("Write permission required");
    }

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
            // Notify current assignee(s) about status change
            var assignments = taskAssignmentRepository.findByTask_Id(task.getId());
            for (var a : assignments) {
                if (a.getUser() != null && a.getUser().getEmail() != null) {
                    notificationService.notifyUserForTask(a.getUser().getEmail(), task.getId(),
                            "Le statut de la tâche '" + task.getName() + "' est passé à '" + req.getStatus() + "'.");
                }
            }
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

    @Transactional(readOnly = true)
    public java.util.List<TaskListItem> list(Integer projectId, String requesterEmail, String assigneeEmail) {
        var requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new UserNotFoundException(requesterEmail));
        boolean isMember = projectMemberRepository.findByProject_IdAndUser_Email(projectId, requester.getEmail()).isPresent();
        if (!isMember) throw new NotProjectMemberException(projectId, requester.getEmail());

        java.util.List<Task> tasks;
        if (assigneeEmail != null && !assigneeEmail.isBlank()) {
            var ids = taskAssignmentRepository.findTaskIdsByProjectIdAndAssigneeEmail(projectId, assigneeEmail);
            tasks = ids.isEmpty() ? java.util.List.of() : taskRepository.findByProject_IdAndIdIn(projectId, ids);
        } else {
            tasks = taskRepository.findByProject_Id(projectId);
        }
        var fmt = new java.text.SimpleDateFormat("yyyy-MM-dd");
        // Build assignment map (taskId -> assigneeEmail) to preselect current assignee in UI
        java.util.Map<Integer, String> assignmentMap = new java.util.HashMap<>();
        if (!tasks.isEmpty()) {
            var taskIds = tasks.stream().map(Task::getId).toList();
            var tas = taskAssignmentRepository.findByTask_IdIn(taskIds);
            for (var ta : tas) {
                if (ta.getTask() != null) {
                    var email = ta.getUser() != null ? ta.getUser().getEmail() : null;
                    assignmentMap.put(ta.getTask().getId(), email);
                }
            }
        }
    return tasks.stream()
        .map(t -> {
            TaskListItem item = new TaskListItem(
                t.getId(), t.getName(), t.getStatus(), t.getPriority(),
                t.getDueDate() != null ? fmt.format(t.getDueDate()) : null);
            item.description = t.getDescription();
            item.assigneeEmail = assignmentMap.get(t.getId());
            return item;
        })
        .toList();
    }

    @Transactional(readOnly = true)
    public TaskBoardResponse board(Integer projectId, String requesterEmail) {
        var items = list(projectId, requesterEmail, null);
        java.util.Map<String, java.util.List<TaskListItem>> lanes = new java.util.HashMap<>();
        for (var it : items) {
            lanes.computeIfAbsent(it.status != null ? it.status : "UNKNOWN", k -> new java.util.ArrayList<>()).add(it);
        }
        return new TaskBoardResponse(lanes);
    }

    @Transactional(readOnly = true)
    public TaskResponse get(Integer taskId, Integer projectId, String requesterEmail) {
        var requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new UserNotFoundException(requesterEmail));
        boolean isMember = projectMemberRepository.findByProject_IdAndUser_Email(projectId, requester.getEmail()).isPresent();
        if (!isMember) throw new NotProjectMemberException(projectId, requester.getEmail());

        var task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
        if (task.getProject() == null || task.getProject().getId() == null || !task.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Task does not belong to project " + projectId);
        }
        return toResponse(task);
    }

    @Transactional(readOnly = true)
    public java.util.List<TaskHistory> history(Integer taskId, Integer projectId, String requesterEmail) {
        var requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new UserNotFoundException(requesterEmail));
        boolean isMember = projectMemberRepository.findByProject_IdAndUser_Email(projectId, requester.getEmail()).isPresent();
        if (!isMember) throw new NotProjectMemberException(projectId, requester.getEmail());

        var task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
        if (task.getProject() == null || task.getProject().getId() == null || !task.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Task does not belong to project " + projectId);
        }
        return taskHistoryRepository.findByTask_IdOrderByChangeDateDesc(taskId);
    }

    @Transactional
    public void delete(Integer taskId, Integer projectId, String requesterEmail) {
        var requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new UserNotFoundException(requesterEmail));
        var pmOpt = projectMemberRepository.findByProject_IdAndUser_Email(projectId, requester.getEmail());
        if (pmOpt.isEmpty()) throw new NotProjectMemberException(projectId, requester.getEmail());
        var roleName = pmOpt.get().getRole() != null ? pmOpt.get().getRole().getName() : null;
        if (!"Administrateur".equalsIgnoreCase(roleName) && !"Membre".equalsIgnoreCase(roleName)) {
            throw new com.pmt.backend.exception.InsufficientProjectPermissionException("Write permission required");
        }

        var task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
        if (task.getProject() == null || task.getProject().getId() == null || !task.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Task does not belong to project " + projectId);
        }

        // Clean dependent data then delete task
        taskAssignmentRepository.deleteByTask_Id(taskId);
        taskHistoryRepository.deleteByTask_Id(taskId);
        taskRepository.deleteById(taskId);
    }
}
