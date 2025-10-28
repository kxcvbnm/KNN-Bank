package com.knn.knnbank.exceptions;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.knn.knnbank.response.Response;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) 
            throws IOException, ServletException {
        
        Response<?> errorResponse = Response.builder()
            .statusCode(HttpStatus.UNAUTHORIZED.value())  // 401 Unauthorized
            .message(authException.getMessage())
            .build();
                
        response.setContentType("application/json");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
