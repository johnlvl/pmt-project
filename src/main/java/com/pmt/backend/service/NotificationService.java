package com.pmt.backend.service;

import com.pmt.backend.entity.Notification;
import com.pmt.backend.entity.Task;
import com.pmt.backend.entity.User;
import com.pmt.backend.exception.UserNotFoundException;
import com.pmt.backend.repository.NotificationRepository;
import com.pmt.backend.repository.TaskRepository;
import com.pmt.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository,
                               TaskRepository taskRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional
    public void notifyUserForTask(String userEmail, Integer taskId, String message) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new UserNotFoundException(userEmail));
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
        Notification n = new Notification();
        n.setUser(user);
        n.setTask(task);
        n.setMessage(message);
        notificationRepository.save(n);
    }

    @Transactional(readOnly = true)
    public List<Notification> listForUser(String userEmail) {
        // ensure user exists for better 404 feedback
        userRepository.findByEmail(userEmail).orElseThrow(() -> new UserNotFoundException(userEmail));
        return notificationRepository.findByUser_EmailOrderByCreatedAtDesc(userEmail);
    }

    @Transactional
    public void markAsRead(Integer notificationId, String userEmail) {
        // basic ownership check via email match
        var notif = notificationRepository.findById(notificationId).orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        if (notif.getUser() == null || notif.getUser().getEmail() == null || !notif.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("Notification does not belong to user " + userEmail);
        }
        notif.setIsRead(true);
        notificationRepository.save(notif);
    }
}
