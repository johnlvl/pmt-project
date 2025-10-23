package com.pmt.backend.service;

import com.pmt.backend.dto.TaskAssignRequest;
import com.pmt.backend.entity.Project;
import com.pmt.backend.entity.ProjectMember;
import com.pmt.backend.entity.Role;
import com.pmt.backend.entity.Task;
import com.pmt.backend.entity.User;
import com.pmt.backend.repository.ProjectMemberRepository;
import com.pmt.backend.repository.TaskAssignmentRepository;
import com.pmt.backend.repository.TaskRepository;
import com.pmt.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.Optional;

import static org.mockito.Mockito.verify;

class TaskAssignmentServiceTest {
    TaskAssignmentRepository taskAssignmentRepository;
    TaskRepository taskRepository;
    UserRepository userRepository;
    ProjectMemberRepository projectMemberRepository;
    NotificationService notificationService;
    EmailService emailService;
    TaskAssignmentService service;

    @BeforeEach
    void setup() {
        taskAssignmentRepository = Mockito.mock(TaskAssignmentRepository.class);
        taskRepository = Mockito.mock(TaskRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        projectMemberRepository = Mockito.mock(ProjectMemberRepository.class);
    notificationService = Mockito.mock(NotificationService.class);
    emailService = Mockito.mock(EmailService.class);
    service = new TaskAssignmentService(taskAssignmentRepository, taskRepository, userRepository, projectMemberRepository, notificationService, emailService);
    }

    @Test
    void assign_sendsNotificationToAssignee() {
        TaskAssignRequest req = new TaskAssignRequest();
        req.setTaskId(10);
        req.setProjectId(1);
        req.setRequesterEmail("alice@example.com");
        req.setAssigneeEmail("bob@example.com");

        User requester = new User(); requester.setId(1); requester.setEmail("alice@example.com");
        User assignee = new User(); assignee.setId(2); assignee.setEmail("bob@example.com");
        Project p = new Project(); p.setId(1);
        Task t = new Task(); t.setId(10); t.setProject(p); t.setName("Task A");

    Mockito.when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(requester));
        Mockito.when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(assignee));
    // requester must have write permission (Admin or Membre)
    ProjectMember requesterMember = new ProjectMember();
    Role requesterRole = new Role();
    requesterRole.setName("Membre");
    requesterMember.setRole(requesterRole);
    Mockito.when(projectMemberRepository.findByProject_IdAndUser_Email(1, "alice@example.com")).thenReturn(Optional.of(requesterMember));
        Mockito.when(projectMemberRepository.findByProject_IdAndUser_Email(1, "bob@example.com")).thenReturn(Optional.of(new ProjectMember()));
        Mockito.when(taskRepository.findById(10)).thenReturn(Optional.of(t));

        service.assign(req);

    verify(notificationService).notifyUserForTask(ArgumentMatchers.eq("bob@example.com"), ArgumentMatchers.eq(10), ArgumentMatchers.contains("Task A"));
    verify(emailService).send(ArgumentMatchers.eq("bob@example.com"), ArgumentMatchers.contains("assignation"), ArgumentMatchers.contains("Task A"));
    }
}
