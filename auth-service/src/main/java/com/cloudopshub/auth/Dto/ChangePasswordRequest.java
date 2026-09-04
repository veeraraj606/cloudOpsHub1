package com.cloudopshub.auth.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "new password id required")
    @Size(min = 8, max = 100, message = "New password must be between 8 to 100 char")
    private String newPassword;
}
