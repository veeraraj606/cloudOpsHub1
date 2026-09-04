package com.cloudopshub.auth.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email is not valid")
    @Size(max = 255, message = "Email must not exceed 255 char")
    private String email;

    public UpdateProfileRequest(String username, String email) {
        this.username = username;
        this.email = email;
    }


}
