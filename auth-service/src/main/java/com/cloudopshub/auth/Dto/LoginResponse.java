package com.cloudopshub.auth.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {

    private String token;

    private String username;

    private String refreshToken;

    public LoginResponse(String token, String username, String refreshToken) {
        this.token = token;
        this.username = username;
        this.refreshToken = refreshToken;
    }
}
