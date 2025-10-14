package com.pmt.backend.service;

import com.pmt.backend.dto.TaskCreateRequest;
import com.pmt.backend.entity.Project;
import com.pmt.backend.entity.ProjectMember;
import com.pmt.backend.entity.Task;
import com.pmt.backend.entity.User;
import com.pmt.backend.exception.NotProjectMemberException;
import com.pmt.backend.repository.ProjectMemberRepository;
import com.pmt.backend.repository.ProjectRepository;
import com.pmt.backend.repository.TaskRepository;
import com.pmt.backend.repository.UserRepository;
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

    @BeforeEach
    void setUp() {
        taskRepository = Mockito.mock(TaskRepository.class);
        projectRepository = Mockito.mock(ProjectRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        projectMemberRepository = Mockito.mock(ProjectMemberRepository.class);
        taskService = new TaskService(taskRepository, projectRepository, userRepository, projectMemberRepository);
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
        Mockito.when(projectMemberRepository.findByProject_IdAndUser_Email(1, "alice@example.com"))
                .thenReturn(Optional.of(new ProjectMember()));
        Mockito.when(projectRepository.findById(1)).thenReturn(Optional.of(p));

        Task saved = new Task(); saved.setId(10); saved.setProject(p); saved.setName("Task A"); saved.setPriority("MEDIUM"); saved.setStatus("TODO");
        Mockito.when(taskRepository.save(Mockito.any(Task.class))).thenReturn(saved);

        var resp = taskService.create(req);
        assertEquals(10, resp.getId());
        assertEquals("TODO", resp.getStatus());
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
}
