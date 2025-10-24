package com.pmt.backend.repository;

import com.pmt.backend.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {
	List<Task> findByProject_Id(Integer projectId);
	List<Task> findByProject_IdAndIdIn(Integer projectId, Collection<Integer> ids);
}
