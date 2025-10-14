package com.pmt.backend.repository;

import com.pmt.backend.entity.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Integer> {
    void deleteByTask_Id(Integer taskId);
}
