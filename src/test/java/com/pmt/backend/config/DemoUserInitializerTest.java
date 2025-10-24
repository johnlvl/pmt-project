package com.pmt.backend.config;

import com.pmt.backend.dto.UserRegistrationRequest;
import com.pmt.backend.repository.UserRepository;
import com.pmt.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DemoUserInitializerTest {

    private UserRepository userRepository;
    private UserService userService;
    private DemoUserInitializer initializer;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = mock(UserService.class);
        initializer = new DemoUserInitializer(userRepository, userService);

        // Default demo user values to assert against
        ReflectionTestUtils.setField(initializer, "enabled", true);
        ReflectionTestUtils.setField(initializer, "email", "demo@example.com");
        ReflectionTestUtils.setField(initializer, "username", "Demo");
        ReflectionTestUtils.setField(initializer, "password", "secret");
    }

    @Test
    void whenDisabled_thenDoesNothing() {
        ReflectionTestUtils.setField(initializer, "enabled", false);

        initializer.run(mock(ApplicationArguments.class));

        verifyNoInteractions(userRepository, userService);
    }

    @Test
    void whenUserAlreadyExists_thenDoesNotRegisterAgain() {
        when(userRepository.existsByEmail("demo@example.com")).thenReturn(true);

        initializer.run(mock(ApplicationArguments.class));

        verify(userRepository).existsByEmail("demo@example.com");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(userService);
    }

    @Test
    void whenUserMissing_thenRegistersWithConfiguredValues() {
        when(userRepository.existsByEmail("demo@example.com")).thenReturn(false);

        initializer.run(mock(ApplicationArguments.class));

        ArgumentCaptor<UserRegistrationRequest> req = ArgumentCaptor.forClass(UserRegistrationRequest.class);
        verify(userService).register(req.capture());
        UserRegistrationRequest captured = req.getValue();
        assertEquals("demo@example.com", captured.getEmail());
        assertEquals("Demo", captured.getUsername());
        assertEquals("secret", captured.getPassword());
    }

    @Test
    void whenRegistrationThrows_thenInitializerSwallowsAndContinues() {
        when(userRepository.existsByEmail("demo@example.com")).thenReturn(false);
        doThrow(new RuntimeException("boom")).when(userService).register(any(UserRegistrationRequest.class));

        assertDoesNotThrow(() -> initializer.run(mock(ApplicationArguments.class)));

        verify(userService).register(any(UserRegistrationRequest.class));
    }
}
