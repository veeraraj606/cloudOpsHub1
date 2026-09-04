package com.cloudopshub.auth.Dto;

import jakarta.validation.constraints.NotNull;

public class UpdateUserStatusRequest {

    @NotNull(message = "Enable status is required")
    private Boolean enabled;


    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
