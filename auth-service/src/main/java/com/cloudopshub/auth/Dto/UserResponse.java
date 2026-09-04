package com.cloudopshub.auth.Dto;

import com.cloudopshub.auth.Repository.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserResponse{

    private Long id;
    private String username;
    private String email;
    private String role;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserResponse(Long id, String username, String email, String role, boolean enabled, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
