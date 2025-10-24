package com.pmt.backend.service;

import com.pmt.backend.dto.TaskListItem;
import com.pmt.backend.entity.Project;
import com.pmt.backend.entity.ProjectMember;
import com.pmt.backend.entity.Task;
import com.pmt.backend.entity.User;
import com.pmt.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TaskServiceListBoardTest {
    TaskRepository taskRepository;
    ProjectRepository projectRepository;
    UserRepository userRepository;
    ProjectMemberRepository projectMemberRepository;
    TaskHistoryRepository taskHistoryRepository;
    TaskAssignmentRepository taskAssignmentRepository;
    TaskService taskService;
    NotificationService notificationService;

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

    @Test
    void list_withoutAssignee_returnsProjectTasks() {
        String requester = "alice@example.com";
        User u = new User(); u.setId(1); u.setEmail(requester);
        Mockito.when(userRepository.findByEmail(requester)).thenReturn(Optional.of(u));
        Mockito.when(projectMemberRepository.findByProject_IdAndUser_Email(1, requester))
                .thenReturn(Optional.of(new ProjectMember()));

        Project p = new Project(); p.setId(1);
        Task t1 = new Task(); t1.setId(10); t1.setProject(p); t1.setName("A"); t1.setStatus("TODO"); t1.setPriority("HIGH");
        Task t2 = new Task(); t2.setId(11); t2.setProject(p); t2.setName("B"); t2.setStatus("IN_PROGRESS"); t2.setPriority("LOW");
        Mockito.when(taskRepository.findByProject_Id(1)).thenReturn(List.of(t1, t2));

        var items = taskService.list(1, requester, null);
        assertEquals(2, items.size());
        assertTrue(items.stream().map(i -> i.id).toList().containsAll(List.of(10, 11)));
    }

    @Test
    void list_withAssignee_filtersByAssignee() {
        String requester = "alice@example.com";
        String assignee = "bob@example.com";
        User u = new User(); u.setId(1); u.setEmail(requester);
        Mockito.when(userRepository.findByEmail(requester)).thenReturn(Optional.of(u));
        Mockito.when(projectMemberRepository.findByProject_IdAndUser_Email(1, requester))
                .thenReturn(Optional.of(new ProjectMember()));

        Project p = new Project(); p.setId(1);
        Task t2 = new Task(); t2.setId(11); t2.setProject(p); t2.setName("B"); t2.setStatus("IN_PROGRESS"); t2.setPriority("LOW");
        Mockito.when(taskAssignmentRepository.findTaskIdsByProjectIdAndAssigneeEmail(1, assignee)).thenReturn(List.of(11));
        Mockito.when(taskRepository.findByProject_IdAndIdIn(1, List.of(11))).thenReturn(List.of(t2));

        var items = taskService.list(1, requester, assignee);
        assertEquals(1, items.size());
        assertEquals(11, items.get(0).id);
    }

    @Test
    void board_groupsByStatus() {
        String requester = "alice@example.com";
        User u = new User(); u.setId(1); u.setEmail(requester);
        Mockito.when(userRepository.findByEmail(requester)).thenReturn(Optional.of(u));
        Mockito.when(projectMemberRepository.findByProject_IdAndUser_Email(1, requester))
                .thenReturn(Optional.of(new ProjectMember()));

        Project p = new Project(); p.setId(1);
        Task t1 = new Task(); t1.setId(10); t1.setProject(p); t1.setName("A"); t1.setStatus("TODO");
        Task t2 = new Task(); t2.setId(11); t2.setProject(p); t2.setName("B"); t2.setStatus("IN_PROGRESS");
        Mockito.when(taskRepository.findByProject_Id(1)).thenReturn(List.of(t1, t2));

        var board = taskService.board(1, requester);
        assertTrue(board.getLanes().containsKey("TODO"));
        assertTrue(board.getLanes().containsKey("IN_PROGRESS"));
        List<TaskListItem> laneTodo = board.getLanes().get("TODO");
        assertEquals(10, laneTodo.get(0).id);
    }
}
