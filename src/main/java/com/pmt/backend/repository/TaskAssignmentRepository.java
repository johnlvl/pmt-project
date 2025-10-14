package com.pmt.backend.repository;

import com.pmt.backend.entity.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Integer> {
    void deleteByTask_Id(Integer taskId);

    @Query("select ta.task.id from TaskAssignment ta where ta.task.project.id = :projectId and ta.user.email = :email")
    List<Integer> findTaskIdsByProjectIdAndAssigneeEmail(@Param("projectId") Integer projectId,
                                                         @Param("email") String email);
}
