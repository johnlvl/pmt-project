package com.pmt.backend.service;

import com.pmt.backend.dto.TaskCreateRequest;
import com.pmt.backend.dto.TaskUpdateRequest;
import com.pmt.backend.entity.Project;
import com.pmt.backend.entity.ProjectMember;
import com.pmt.backend.entity.Role;
import com.pmt.backend.entity.Task;
import com.pmt.backend.entity.User;
import com.pmt.backend.exception.NotProjectMemberException;
import com.pmt.backend.repository.ProjectMemberRepository;
import com.pmt.backend.repository.ProjectRepository;
import com.pmt.backend.repository.TaskRepository;
import com.pmt.backend.repository.TaskHistoryRepository;
import com.pmt.backend.repository.TaskAssignmentRepository;
import com.pmt.backend.repository.UserRepository;
import com.pmt.backend.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TaskServiceTest {
    TaskRepository taskRepository;
    ProjectRepository projectRepository;
    UserRepository userRepository;
    ProjectMemberRepository projectMemberRepository;
    TaskService taskService;
    TaskHistoryRepository taskHistoryRepository;
    TaskAssignmentRepository taskAssignmentRepository;
    NotificationService notificationService;

    @BeforeEach
    void setUp() {
    taskRepository = Mockito.mock(TaskRepository.class);
        projectRepository = Mockito.mock(ProjectRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        projectMemberRepository = Mockito.mock(ProjectMemberRepository.class);
    taskHistoryRepository = Mockito.mock(TaskHistoryRepository.class);
    taskAssignmentRepository = Mockito.mock(TaskAssignmentRepository.class);
    notificationService = Mockito.mock(NotificationService.class);
    taskService = new TaskService(taskRepository, projectRepository, userRepository, projectMemberRepository, taskHistoryRepository, taskAssignmentRepository, notificationService);
    }

    @Test
    void create_ok() {
        TaskCreateRequest req = new TaskCreateRequest();
        req.setProjectId(1);
        req.setRequesterEmail("alice@example.com");
        req.setName("Task A");
        req.setPriority("MEDIUM");

        User u = new User(); u.setId(1); u.setEmail("alice@example.com");
        Project p = new Project(); p.setId(1);

    Mockito.when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(u));
    ProjectMember pm = new ProjectMember();
    Role r = new Role(); r.setName("Membre");
    pm.setRole(r);
    Mockito.when(projectMemberRepository.findByProject_IdAndUser_Email(1, "alice@example.com"))
        .thenReturn(Optional.of(pm));
        Mockito.when(projectRepository.findById(1)).thenReturn(Optional.of(p));

        Task saved = new Task(); saved.setId(10); saved.setProject(p); saved.setName("Task A"); saved.setPriority("MEDIUM"); saved.setStatus("TODO");
        Mockito.when(taskRepository.save(Mockito.any(Task.class))).thenReturn(saved);

        var resp = taskService.create(req);
        assertEquals(10, resp.getId());
        assertEquals("TODO", resp.getStatus());
    }

    @Test
    void update_ok() {
        TaskUpdateRequest req = new TaskUpdateRequest();
        req.setTaskId(10);
        req.setProjectId(1);
        req.setRequesterEmail("alice@example.com");
        req.setStatus("IN_PROGRESS");

        User u = new User(); u.setId(1); u.setEmail("alice@example.com");
        Project p = new Project(); p.setId(1);
        Task t = new Task(); t.setId(10); t.setProject(p); t.setStatus("TODO");

    Mockito.when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(u));
    ProjectMember pm = new ProjectMember();
    Role r = new Role(); r.setName("Membre");
    pm.setRole(r);
    Mockito.when(projectMemberRepository.findByProject_IdAndUser_Email(1, "alice@example.com")).thenReturn(Optional.of(pm));
        Mockito.when(taskRepository.findById(10)).thenReturn(Optional.of(t));
        Mockito.when(taskRepository.save(Mockito.any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        var resp = taskService.update(req);
        assertEquals("IN_PROGRESS", resp.getStatus());
    }

    @Test
    void create_forbidden_whenNotMember() {
        TaskCreateRequest req = new TaskCreateRequest();
        req.setProjectId(1);
        req.setRequesterEmail("alice@example.com");
        req.setName("Task A");

        User u = new User(); u.setId(1); u.setEmail("alice@example.com");
        Mockito.when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(u));
        Mockito.when(projectMemberRepository.findByProject_IdAndUser_Email(1, "alice@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(NotProjectMemberException.class, () -> taskService.create(req));
    }

    @Test
    void create_invalidDueDate_throwsIllegalArgument() {
        TaskCreateRequest req = new TaskCreateRequest();
        req.setProjectId(1);
        req.setRequesterEmail("alice@example.com");
        req.setName("Task A");
        req.setPriority("LOW");
    // Invalid format to trigger parse error branch
    req.setDueDate("2025/01/01");

        User u = new User(); u.setId(1); u.setEmail("alice@example.com");
        Project p = new Project(); p.setId(1);

        Mockito.when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(u));
        ProjectMember pm = new ProjectMember();
        Role r = new Role(); r.setName("Administrateur");
        pm.setRole(r);
        Mockito.when(projectMemberRepository.findByProject_IdAndUser_Email(1, "alice@example.com"))
            .thenReturn(Optional.of(pm));
        Mockito.when(projectRepository.findById(1)).thenReturn(Optional.of(p));

        assertThrows(IllegalArgumentException.class, () -> taskService.create(req));
    }

    @Test
    void update_statusChange_notifiesAssignees_and_createsHistory() {
        TaskUpdateRequest req = new TaskUpdateRequest();
        req.setTaskId(10);
        req.setProjectId(1);
        req.setRequesterEmail("alice@example.com");
        req.setStatus("DONE");

        User u = new User(); u.setId(1); u.setEmail("alice@example.com");
        Project p = new Project(); p.setId(1);
        Task t = new Task(); t.setId(10); t.setProject(p); t.setName("X"); t.setStatus("TODO");

        Mockito.when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(u));
        ProjectMember pm = new ProjectMember();
        Role r = new Role(); r.setName("Membre");
        pm.setRole(r);
        Mockito.when(projectMemberRepository.findByProject_IdAndUser_Email(1, "alice@example.com")).thenReturn(Optional.of(pm));
        Mockito.when(taskRepository.findById(10)).thenReturn(Optional.of(t));
        Mockito.when(taskRepository.save(Mockito.any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        // One assignee currently on the task
        com.pmt.backend.entity.TaskAssignment ta = new com.pmt.backend.entity.TaskAssignment();
        ta.setTask(t);
        User assignee = new User(); assignee.setEmail("bob@example.com");
        ta.setUser(assignee);
        Mockito.when(taskAssignmentRepository.findByTask_Id(10)).thenReturn(java.util.List.of(ta));

        var resp = taskService.update(req);
        assertEquals("DONE", resp.getStatus());
        // Notification sent to assignee
        Mockito.verify(notificationService).notifyUserForTask(
            Mockito.eq("bob@example.com"), Mockito.eq(10), Mockito.contains("statut"));
        // History saved because there were changes
        Mockito.verify(taskHistoryRepository).save(Mockito.any());
    }

    @Test
    void update_noChanges_noHistorySaved() {
        TaskUpdateRequest req = new TaskUpdateRequest();
        req.setTaskId(10);
        req.setProjectId(1);
        req.setRequesterEmail("alice@example.com");
        // All fields null -> no change

        User u = new User(); u.setId(1); u.setEmail("alice@example.com");
        Project p = new Project(); p.setId(1);
        Task t = new Task();
        t.setId(10); t.setProject(p); t.setName("Same"); t.setDescription("Desc"); t.setPriority("LOW"); t.setStatus("TODO");

        Mockito.when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(u));
        ProjectMember pm = new ProjectMember();
        Role r = new Role(); r.setName("Membre");
        pm.setRole(r);
        Mockito.when(projectMemberRepository.findByProject_IdAndUser_Email(1, "alice@example.com")).thenReturn(Optional.of(pm));
        Mockito.when(taskRepository.findById(10)).thenReturn(Optional.of(t));
        Mockito.when(taskRepository.save(Mockito.any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        var resp = taskService.update(req);
        assertEquals("TODO", resp.getStatus());
        // Ensure we did not write a TaskHistory when nothing changed
        Mockito.verify(taskHistoryRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void delete_observer_forbidden() {
        String requester = "obs@example.com";
        User u = new User(); u.setId(1); u.setEmail(requester);
        Project p = new Project(); p.setId(1);
        Task t = new Task(); t.setId(10); t.setProject(p);

        Mockito.when(userRepository.findByEmail(requester)).thenReturn(Optional.of(u));
        ProjectMember pm = new ProjectMember();
        Role r = new Role(); r.setName("Observateur");
        pm.setRole(r);
        Mockito.when(projectMemberRepository.findByProject_IdAndUser_Email(1, requester)).thenReturn(Optional.of(pm));
        Mockito.when(taskRepository.findById(10)).thenReturn(Optional.of(t));

        assertThrows(com.pmt.backend.exception.InsufficientProjectPermissionException.class,
            () -> taskService.delete(10, 1, requester));
        // No deletion should happen
        Mockito.verify(taskRepository, Mockito.never()).deleteById(Mockito.any());
    }

    @Test
    void list_notMember_throws() {
        String requester = "notmember@example.com";
        User u = new User(); u.setId(1); u.setEmail(requester);
        Mockito.when(userRepository.findByEmail(requester)).thenReturn(Optional.of(u));
        Mockito.when(projectMemberRepository.findByProject_IdAndUser_Email(1, requester))
            .thenReturn(Optional.empty());

        assertThrows(NotProjectMemberException.class, () -> taskService.list(1, requester, null));
    }
}
