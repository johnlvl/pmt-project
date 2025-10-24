package com.pmt.backend.repository;

import com.pmt.backend.entity.ProjectInvitation;
import com.pmt.backend.entity.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, Integer> {
	// default JpaRepository provides findById

	List<ProjectInvitation> findByEmailAndStatusOrderByCreatedAtDesc(String email, InvitationStatus status);
}
