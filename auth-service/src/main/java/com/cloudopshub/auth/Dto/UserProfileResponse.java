package com.cloudopshub.auth.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileResponse {

    private Long id;
    private String username;
    private String email;
    private String role;

    public UserProfileResponse(Long id, String username, String email, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }
}
