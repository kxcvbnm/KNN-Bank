package com.knn.knnbank.auth_users.service;

import com.knn.knnbank.auth_users.dtos.LoginRequest;
import com.knn.knnbank.auth_users.dtos.LoginResponse;
import com.knn.knnbank.auth_users.dtos.RegisterRequest;
import com.knn.knnbank.auth_users.dtos.ResetPasswordRequest;
import com.knn.knnbank.response.Response;

public interface AuthService {
    
    Response<String> register(RegisterRequest registerRequest);
    
    Response<LoginResponse> login(LoginRequest loginRequest);
    
    Response<?> forgotPassword(String email);
    
    Response<?> updatePasswordViaResetCode(ResetPasswordRequest resetPasswordRequest);
}
