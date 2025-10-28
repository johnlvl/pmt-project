package com.pmt.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import com.pmt.backend.repository.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
	"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
@ActiveProfiles("test")
class BackendApplicationTests {

	// Mock all repositories to avoid JPA wiring during context bootstrap
	@MockBean private ProjectRepository projectRepository;
	@MockBean private ProjectMemberRepository projectMemberRepository;
	@MockBean private ProjectInvitationRepository projectInvitationRepository;
	@MockBean private RoleRepository roleRepository;
	@MockBean private TaskRepository taskRepository;
	@MockBean private TaskAssignmentRepository taskAssignmentRepository;
	@MockBean private TaskHistoryRepository taskHistoryRepository;
	@MockBean private NotificationRepository notificationRepository;
	@MockBean private UserRepository userRepository;

	@Test
	void contextLoads() {
	}

}
