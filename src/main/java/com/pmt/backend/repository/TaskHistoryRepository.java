package com.pmt.backend.repository;

import com.pmt.backend.entity.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Integer> {
}
