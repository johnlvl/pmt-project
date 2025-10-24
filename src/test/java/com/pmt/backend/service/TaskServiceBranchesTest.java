package com.pmt.backend.service;

import com.pmt.backend.dto.TaskUpdateRequest;
import com.pmt.backend.entity.*;
import com.pmt.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TaskServiceBranchesTest {
    TaskRepository taskRepository;
    ProjectRepository projectRepository;
    UserRepository userRepository;
    ProjectMemberRepository projectMemberRepository;
    TaskHistoryRepository taskHistoryRepository;
    TaskAssignmentRepository taskAssignmentRepository;
    NotificationService notificationService;
    TaskService taskService;

    @BeforeEach
    void setup() {
        taskRepository = Mockito.mock(TaskRepository.class);
        projectRepository = Mockito.mock(ProjectRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        projectMemberRepository = Mockito.mock(ProjectMemberRepository.class);
        taskHistoryRepository = Mockito.mock(TaskHistoryRepository.class);
        taskAssignmentRepository = Mockito.mock(TaskAssignmentRepository.class);
        notificationService = Mockito.mock(NotificationService.class);
        taskService = new TaskService(taskRepository, projectRepository, userRepository,
            projectMemberRepository, taskHistoryRepository, taskAssignmentRepository, notificationService);
    }

    private ProjectMember asMember() {
        ProjectMember pm = new ProjectMember();
        Role r = new Role(); r.setName("Membre");
        pm.setRole(r);
        return pm;
    }

    @Test
    void update_taskNotInProject_throws() {
        String requester = "alice@example.com";
        User u = new User(); u.setEmail(requester);
        Mockito.when(userRepository.findByEmail(requester)).thenReturn(Optional.of(u));
        Mockito.when(projectMemberRepository.findByProject_IdAndUser_Email(2, requester)).thenReturn(Optional.of(asMember()));

        Project p = new Project(); p.setId(1);
        Task t = new Task(); t.setId(10); t.setProject(p); t.setStatus("TODO");
        Mockito.when(taskRepository.findById(10)).thenReturn(Optional.of(t));

        TaskUpdateRequest req = new TaskUpdateRequest();
        req.setTaskId(10); req.setProjectId(2); req.setRequesterEmail(requester);
        req.setStatus("IN_PROGRESS");

        assertThrows(IllegalArgumentException.class, () -> taskService.update(req));
    }

    @Test
    void update_dueDate_set_and_endDate_clear_createsHistory() throws Exception {
        String requester = "alice@example.com";
        User u = new User(); u.setEmail(requester);
        Mockito.when(userRepository.findByEmail(requester)).thenReturn(Optional.of(u));
        Mockito.when(projectMemberRepository.findByProject_IdAndUser_Email(1, requester)).thenReturn(Optional.of(asMember()));

        Project p = new Project(); p.setId(1);
        Task t = new Task(); t.setId(10); t.setProject(p); t.setName("Task"); t.setStatus("TODO");
        // Pre-set an endDate so that clearing it is a change
        t.setEndDate(new SimpleDateFormat("yyyy-MM-dd").parse("2025-01-02"));
        Mockito.when(taskRepository.findById(10)).thenReturn(Optional.of(t));
        Mockito.when(taskRepository.save(Mockito.any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskUpdateRequest req = new TaskUpdateRequest();
        req.setTaskId(10); req.setProjectId(1); req.setRequesterEmail(requester);
        req.setDueDate("2025-01-01"); // set new due date
        req.setEndDate(""); // clear end date

        var resp = taskService.update(req);
        assertEquals("2025-01-01", resp.getDueDate());
        assertNull(resp.getEndDate());
        Mockito.verify(taskHistoryRepository).save(Mockito.any());
    }

    @Test
    void list_withAssignee_noIds_returnsEmpty() {
        String requester = "alice@example.com";
        User u = new User(); u.setEmail(requester);
        Mockito.when(userRepository.findByEmail(requester)).thenReturn(Optional.of(u));
        Mockito.when(projectMemberRepository.findByProject_IdAndUser_Email(1, requester)).thenReturn(Optional.of(asMember()));

        Mockito.when(taskAssignmentRepository.findTaskIdsByProjectIdAndAssigneeEmail(1, "bob@example.com"))
            .thenReturn(List.of());

        var items = taskService.list(1, requester, "bob@example.com");
        assertTrue(items.isEmpty());
        Mockito.verify(taskRepository, Mockito.never()).findByProject_IdAndIdIn(Mockito.anyInt(), Mockito.any());
    }

    @Test
    void get_taskNotInProject_throws() {
        String requester = "alice@example.com";
        User u = new User(); u.setEmail(requester);
        Mockito.when(userRepository.findByEmail(requester)).thenReturn(Optional.of(u));
        Mockito.when(projectMemberRepository.findByProject_IdAndUser_Email(2, requester)).thenReturn(Optional.of(asMember()));

        Project p = new Project(); p.setId(1);
        Task t = new Task(); t.setId(10); t.setProject(p);
        Mockito.when(taskRepository.findById(10)).thenReturn(Optional.of(t));

        assertThrows(IllegalArgumentException.class, () -> taskService.get(10, 2, requester));
    }

    @Test
    void history_taskNotInProject_throws() {
        String requester = "alice@example.com";
        User u = new User(); u.setEmail(requester);
        Mockito.when(userRepository.findByEmail(requester)).thenReturn(Optional.of(u));
        Mockito.when(projectMemberRepository.findByProject_IdAndUser_Email(2, requester)).thenReturn(Optional.of(asMember()));

        Project p = new Project(); p.setId(1);
        Task t = new Task(); t.setId(10); t.setProject(p);
        Mockito.when(taskRepository.findById(10)).thenReturn(Optional.of(t));

        assertThrows(IllegalArgumentException.class, () -> taskService.history(10, 2, requester));
    }

    @Test
    void delete_admin_success_cleansDependencies() {
        String requester = "admin@example.com";
        User u = new User(); u.setEmail(requester);
        Mockito.when(userRepository.findByEmail(requester)).thenReturn(Optional.of(u));
        ProjectMember pm = new ProjectMember();
        Role r = new Role(); r.setName("Administrateur");
        pm.setRole(r);
        Mockito.when(projectMemberRepository.findByProject_IdAndUser_Email(1, requester)).thenReturn(Optional.of(pm));

        Project p = new Project(); p.setId(1);
        Task t = new Task(); t.setId(10); t.setProject(p);
        Mockito.when(taskRepository.findById(10)).thenReturn(Optional.of(t));

        taskService.delete(10, 1, requester);

        Mockito.verify(taskAssignmentRepository).deleteByTask_Id(10);
        Mockito.verify(taskHistoryRepository).deleteByTask_Id(10);
        Mockito.verify(taskRepository).deleteById(10);
    }

    @Test
    void update_changeNameDescriptionPriority_recordsHistory() {
        String requester = "alice@example.com";
        User u = new User(); u.setEmail(requester);
        Mockito.when(userRepository.findByEmail(requester)).thenReturn(Optional.of(u));
        Mockito.when(projectMemberRepository.findByProject_IdAndUser_Email(1, requester)).thenReturn(Optional.of(asMember()));

        Project p = new Project(); p.setId(1);
        Task t = new Task(); t.setId(10); t.setProject(p);
        t.setName("Old"); t.setDescription("Desc"); t.setPriority("LOW"); t.setStatus("TODO");
        Mockito.when(taskRepository.findById(10)).thenReturn(Optional.of(t));
        Mockito.when(taskRepository.save(Mockito.any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskUpdateRequest req = new TaskUpdateRequest();
        req.setTaskId(10); req.setProjectId(1); req.setRequesterEmail(requester);
        req.setName("New"); req.setDescription("Desc2"); req.setPriority("HIGH");

        var resp = taskService.update(req);
        assertEquals("New", resp.getName());
        assertEquals("Desc2", resp.getDescription());
        assertEquals("HIGH", resp.getPriority());
        Mockito.verify(taskHistoryRepository).save(Mockito.any());
    }

    @Test
    void list_populatesAssigneeEmails_viaAssignmentMap() {
        String requester = "alice@example.com";
        User u = new User(); u.setEmail(requester);
        Mockito.when(userRepository.findByEmail(requester)).thenReturn(Optional.of(u));
        Mockito.when(projectMemberRepository.findByProject_IdAndUser_Email(1, requester)).thenReturn(Optional.of(asMember()));

        Project p = new Project(); p.setId(1);
        Task t1 = new Task(); t1.setId(10); t1.setProject(p); t1.setName("A");
        Task t2 = new Task(); t2.setId(11); t2.setProject(p); t2.setName("B");
        Mockito.when(taskRepository.findByProject_Id(1)).thenReturn(List.of(t1, t2));

        TaskAssignment ta1 = new TaskAssignment(); ta1.setTask(t1); User u1 = new User(); u1.setEmail("bob@example.com"); ta1.setUser(u1);
        TaskAssignment ta2 = new TaskAssignment(); ta2.setTask(t2); User u2 = new User(); u2.setEmail("carol@example.com"); ta2.setUser(u2);
        Mockito.when(taskAssignmentRepository.findByTask_IdIn(List.of(10, 11))).thenReturn(List.of(ta1, ta2));

        var items = taskService.list(1, requester, null);
        assertEquals(2, items.size());
        var map = items.stream().collect(java.util.stream.Collectors.toMap(i -> i.id, i -> i.assigneeEmail));
        assertEquals("bob@example.com", map.get(10));
        assertEquals("carol@example.com", map.get(11));
    }
}
