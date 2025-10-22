package com.pmt.backend.config;

import com.pmt.backend.entity.Role;
import com.pmt.backend.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Profile("!test")
public class RoleInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(RoleInitializer.class);
    private final RoleRepository roleRepository;

    public RoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> required = Arrays.asList("Administrateur", "Mainteneur", "Membre");
        for (String name : required) {
            roleRepository.findByName(name).orElseGet(() -> {
                Role r = new Role();
                r.setName(name);
                Role saved = roleRepository.save(r);
                log.info("Seeded role: {} (id={})", name, saved.getId());
                return saved;
            });
        }
    }
}
