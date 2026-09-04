package com.cloudopshub.auth.Service;

import com.cloudopshub.auth.Dto.LoginRequest;
import com.cloudopshub.auth.Dto.LoginResponse;
import com.cloudopshub.auth.Dto.RegisterRequest;
import com.cloudopshub.auth.Dto.UserResponse;
import com.cloudopshub.auth.Entity.RefreshToken;
import com.cloudopshub.auth.Entity.User;
import com.cloudopshub.auth.Repository.UserRepository;
import com.cloudopshub.auth.Security.JwtService;
import com.cloudopshub.auth.exception.DuplicateResourceException;
import com.cloudopshub.auth.exception.InvalidCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("veera1");
        existingUser.setEmail("veera1@example.com");
        existingUser.setPassword("encoded-password");
        existingUser.setRole("User");
        existingUser.setEnabled(true);
        existingUser.setCreatedAt(LocalDateTime.now());
        existingUser.setUpdatedAt(LocalDateTime.now());
    }

    // ---------- register() ----------

    @Test
    void register_success_returnsUserResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("plainPassword123");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword123")).thenReturn("encoded-password");

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername("newuser");
        savedUser.setEmail("newuser@example.com");
        savedUser.setPassword("encoded-password");
        savedUser.setRole("User");
        savedUser.setEnabled(true);
        savedUser.setCreatedAt(LocalDateTime.now());
        savedUser.setUpdatedAt(LocalDateTime.now());

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = authService.register(request);

        assertThat(response.getUsername()).isEqualTo("newuser");
        assertThat(response.getEmail()).isEqualTo("newuser@example.com");
        assertThat(response.getRole()).isEqualTo("User");
        assertThat(response.isEnabled()).isTrue();

        // password must never be passed through as plain text
        verify(passwordEncoder).encode("plainPassword123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateUsername_throwsDuplicateResourceException() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("veera1");
        request.setEmail("someoneelse@example.com");
        request.setPassword("plainPassword123");

        when(userRepository.existsByUsername("veera1")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Username already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsDuplicateResourceException() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("brandnewuser");
        request.setEmail("veera1@example.com");
        request.setPassword("plainPassword123");

        when(userRepository.existsByUsername("brandnewuser")).thenReturn(false);
        when(userRepository.existsByEmail("veera1@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    // ---------- login() ----------

    @Test
    void login_success_returnsLoginResponseWithTokenAndRefreshToken() {
        LoginRequest request = new LoginRequest();
        request.setUsername("veera1");
        request.setPassword("correctPassword");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token-value");
        refreshToken.setUser(existingUser);

        when(userRepository.findByUsername("veera1")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("correctPassword", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken("veera1", "User")).thenReturn("jwt-access-token");
        when(refreshTokenService.createRefreshToken(existingUser)).thenReturn(refreshToken);

        LoginResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-access-token");
        assertThat(response.getUsername()).isEqualTo("veera1");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-value");
    }

    @Test
    void login_unknownUsername_throwsInvalidCredentialsException() {
        LoginRequest request = new LoginRequest();
        request.setUsername("doesNotExist");
        request.setPassword("whatever");

        when(userRepository.findByUsername("doesNotExist")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid username or password");

        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    @Test
    void login_disabledAccount_throwsInvalidCredentialsException() {
        existingUser.setEnabled(false);

        LoginRequest request = new LoginRequest();
        request.setUsername("veera1");
        request.setPassword("correctPassword");

        when(userRepository.findByUsername("veera1")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("user account is disabled");

        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentialsException() {
        LoginRequest request = new LoginRequest();
        request.setUsername("veera1");
        request.setPassword("wrongPassword");

        when(userRepository.findByUsername("veera1")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongPassword", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid username or password");

        verify(jwtService, never()).generateToken(anyString(), anyString());
    }
}