package com.pmt.backend.repository;

import com.pmt.backend.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Integer> {
	boolean existsByProject_IdAndUser_Id(Integer projectId, Integer userId);
	java.util.Optional<com.pmt.backend.entity.ProjectMember> findByProject_IdAndUser_Email(Integer projectId, String email);
}
