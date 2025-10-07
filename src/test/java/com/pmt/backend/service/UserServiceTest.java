package com.pmt.backend.service;

import com.pmt.backend.dto.UserLoginRequest;
import com.pmt.backend.dto.UserRegistrationRequest;
import com.pmt.backend.dto.UserResponse;
import com.pmt.backend.entity.User;
import com.pmt.backend.exception.EmailAlreadyUsedException;
import com.pmt.backend.exception.InvalidCredentialsException;
import com.pmt.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private BCryptPasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        encoder = new BCryptPasswordEncoder();
    }

    @Test
    void register_shouldCreateUser_whenEmailNotExists() {
        UserRegistrationRequest req = new UserRegistrationRequest();
        req.setUsername("alice");
        req.setEmail("alice@example.com");
        req.setPassword("Password123!");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            // simulate DB assigning ID
            u.setId(1);
            return u;
        });

        UserResponse res = userService.register(req);

        assertNotNull(res);
        assertEquals(1, res.getId());
        assertEquals("alice", res.getUsername());
        assertEquals("alice@example.com", res.getEmail());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertNotEquals("Password123!", saved.getPassword(), "Password must be hashed");
        assertTrue(saved.getPassword().startsWith("$2"), "Should be BCrypt hash");
    }

    @Test
    void register_shouldThrow_whenEmailExists() {
        UserRegistrationRequest req = new UserRegistrationRequest();
        req.setUsername("bob");
        req.setEmail("bob@example.com");
        req.setPassword("Secret123!");

        when(userRepository.existsByEmail("bob@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyUsedException.class, () -> userService.register(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldReturnUser_whenCredentialsAreValid() {
        String raw = "Password123!";
        String hash = encoder.encode(raw);
        User user = new User();
        user.setId(42);
        user.setUsername("carol");
        user.setEmail("carol@example.com");
        user.setPassword(hash);

        when(userRepository.findByEmail("carol@example.com")).thenReturn(Optional.of(user));

        UserLoginRequest req = new UserLoginRequest();
        req.setEmail("carol@example.com");
        req.setPassword(raw);

        UserResponse res = userService.login(req);

        assertEquals(42, res.getId());
        assertEquals("carol", res.getUsername());
        assertEquals("carol@example.com", res.getEmail());
    }

    @Test
    void login_shouldThrow_whenPasswordInvalid() {
        String hash = encoder.encode("CorrectPassword!");
        User user = new User();
        user.setId(7);
        user.setUsername("dave");
        user.setEmail("dave@example.com");
        user.setPassword(hash);

        when(userRepository.findByEmail("dave@example.com")).thenReturn(Optional.of(user));

        UserLoginRequest req = new UserLoginRequest();
        req.setEmail("dave@example.com");
        req.setPassword("WrongPassword!");

        assertThrows(InvalidCredentialsException.class, () -> userService.login(req));
    }
}
