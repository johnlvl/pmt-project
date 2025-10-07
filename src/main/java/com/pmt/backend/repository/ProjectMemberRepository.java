package com.pmt.backend.repository;

import com.pmt.backend.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Integer> {
	boolean existsByProject_IdAndUser_Id(Integer projectId, Integer userId);
}
