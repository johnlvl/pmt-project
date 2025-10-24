package com.pmt.backend.service;

import com.pmt.backend.entity.Notification;
import com.pmt.backend.entity.Task;
import com.pmt.backend.entity.User;
import com.pmt.backend.exception.UserNotFoundException;
import com.pmt.backend.repository.NotificationRepository;
import com.pmt.backend.repository.TaskRepository;
import com.pmt.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {
    NotificationRepository notificationRepository;
    UserRepository userRepository;
    TaskRepository taskRepository;
    NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationRepository = Mockito.mock(NotificationRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        taskRepository = Mockito.mock(TaskRepository.class);
        notificationService = new NotificationService(notificationRepository, userRepository, taskRepository);
    }

    @Test
    void notifyUserForTask_persistsNotification() {
        String email = "alice@example.com";
        Integer taskId = 42;

        User u = new User(); u.setId(1); u.setEmail(email);
        Task t = new Task(); t.setId(taskId);

        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(u));
        Mockito.when(taskRepository.findById(taskId)).thenReturn(Optional.of(t));

        notificationService.notifyUserForTask(email, taskId, "Assigned to you");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        Mockito.verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertEquals(u, saved.getUser());
        assertEquals(t, saved.getTask());
        assertEquals("Assigned to you", saved.getMessage());
        assertFalse(Boolean.TRUE.equals(saved.getIsRead()));
    }

    @Test
    void listForUser_returnsRepositoryResults() {
        String email = "alice@example.com";
        User u = new User(); u.setId(1); u.setEmail(email);
        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(u));

        Notification n1 = new Notification(); n1.setId(1);
        Notification n2 = new Notification(); n2.setId(2);
        Mockito.when(notificationRepository.findByUser_EmailOrderByCreatedAtDesc(email))
                .thenReturn(List.of(n1, n2));

        var results = notificationService.listForUser(email);
        assertEquals(2, results.size());
        assertEquals(1, results.get(0).getId());
        assertEquals(2, results.get(1).getId());
    }

    @Test
    void listForUser_userNotFound_throws() {
        Mockito.when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> notificationService.listForUser("ghost@example.com"));
        Mockito.verifyNoInteractions(notificationRepository);
    }

    @Test
    void markAsRead_happyPath_setsFlagAndSaves() {
        String email = "alice@example.com";
        User u = new User(); u.setEmail(email);
        Notification n = new Notification(); n.setId(99); n.setUser(u); n.setIsRead(false);
        Mockito.when(notificationRepository.findById(99)).thenReturn(Optional.of(n));

        notificationService.markAsRead(99, email);

        assertTrue(n.getIsRead());
        Mockito.verify(notificationRepository).save(n);
    }

    @Test
    void markAsRead_wrongOwner_throwsAndDoesNotSave() {
        String email = "alice@example.com";
        User owner = new User(); owner.setEmail("bob@example.com");
        Notification n = new Notification(); n.setId(100); n.setUser(owner); n.setIsRead(false);
        Mockito.when(notificationRepository.findById(100)).thenReturn(Optional.of(n));

        assertThrows(IllegalArgumentException.class, () -> notificationService.markAsRead(100, email));
        Mockito.verify(notificationRepository, Mockito.never()).save(Mockito.any());
    }
}
