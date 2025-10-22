package com.pmt.backend.config;

import com.pmt.backend.dto.UserRegistrationRequest;
import com.pmt.backend.repository.UserRepository;
import com.pmt.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class DemoUserInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoUserInitializer.class);

    private final UserRepository userRepository;
    private final UserService userService;

    @Value("${pmt.demo-user.enabled:true}")
    private boolean enabled;

    @Value("${pmt.demo-user.email:alice@example.com}")
    private String email;

    @Value("${pmt.demo-user.username:Alice}")
    private String username;

    @Value("${pmt.demo-user.password:password}")
    private String password;

    public DemoUserInitializer(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            log.info("Demo user seeding disabled (pmt.demo-user.enabled=false)");
            return;
        }
        try {
            if (userRepository.existsByEmail(email)) {
                log.info("Demo user already exists: {}", email);
                return;
            }
            UserRegistrationRequest req = new UserRegistrationRequest();
            req.setEmail(email);
            req.setUsername(username);
            req.setPassword(password);
            userService.register(req);
            log.info("Demo user created: {} (username: {})", email, username);
        } catch (Exception e) {
            log.warn("Failed to ensure demo user '{}': {}", email, e.getMessage());
        }
    }
}
