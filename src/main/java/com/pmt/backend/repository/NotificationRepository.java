package com.pmt.backend.repository;

import com.pmt.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUser_EmailOrderByCreatedAtDesc(String email);
}
