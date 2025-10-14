package com.pmt.backend.repository;

import com.pmt.backend.entity.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Integer> {
	List<TaskHistory> findByTask_IdOrderByChangeDateDesc(Integer taskId);
}
