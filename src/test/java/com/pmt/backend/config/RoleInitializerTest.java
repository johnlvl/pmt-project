package com.pmt.backend.config;

import com.pmt.backend.entity.Role;
import com.pmt.backend.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RoleInitializerTest {

    @Test
    void whenAllRolesExist_thenNoSaveIsCalled() {
        RoleRepository repo = mock(RoleRepository.class);
        when(repo.findByName("Administrateur")).thenReturn(Optional.of(new Role()));
        when(repo.findByName("Mainteneur")).thenReturn(Optional.of(new Role()));
        when(repo.findByName("Membre")).thenReturn(Optional.of(new Role()));

        RoleInitializer initializer = new RoleInitializer(repo);
        initializer.run(mock(ApplicationArguments.class));

        verify(repo, times(1)).findByName("Administrateur");
        verify(repo, times(1)).findByName("Mainteneur");
        verify(repo, times(1)).findByName("Membre");
        verify(repo, never()).save(any(Role.class));
    }

    @Test
    void whenRolesMissing_thenTheyAreSeeded() {
        RoleRepository repo = mock(RoleRepository.class);
        when(repo.findByName(any())).thenReturn(Optional.empty());
        when(repo.save(any(Role.class))).thenAnswer(invocation -> {
            Role r = invocation.getArgument(0);
            r.setId(1); // simulate generated id for logging path
            return r;
        });

        RoleInitializer initializer = new RoleInitializer(repo);
        initializer.run(mock(ApplicationArguments.class));

        // verify findByName for each expected role
        verify(repo).findByName(eq("Administrateur"));
        verify(repo).findByName(eq("Mainteneur"));
        verify(repo).findByName(eq("Membre"));
        // and that save was called three times
        verify(repo, times(3)).save(any(Role.class));
    }
}
