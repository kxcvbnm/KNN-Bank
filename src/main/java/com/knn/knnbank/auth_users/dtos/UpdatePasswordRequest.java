package com.knn.knnbank.auth_users.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePasswordRequest {
    
    @NotBlank(message = "Password is required")
    private String oldPassword;

    @NotBlank(message = "New password is required")
    private String newPassword;
}
