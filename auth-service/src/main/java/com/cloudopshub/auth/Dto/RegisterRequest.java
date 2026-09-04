package com.cloudopshub.auth.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message="Username is required")
    @Size(min = 3, max = 100, message = "The username must be between 3 and 100")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email is not valid")
    private String email;

    @NotBlank(message = "password id required")
    @Size(min = 8,max = 100, message = "Password must be between 8 to 100")
    private String password;


}
