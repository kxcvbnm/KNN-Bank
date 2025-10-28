package com.knn.knnbank.auth_users.dtos;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    
    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName;

    private String phoneNumber;

    @NotBlank(message = "Email is required")
    private String email;

    private List<String> roles;

    @NotBlank(message = "Password is required")
    private String password;
}
