package com.pmt.backend.repository;

import com.pmt.backend.entity.ProjectInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, Integer> {
	// default JpaRepository provides findById
}
