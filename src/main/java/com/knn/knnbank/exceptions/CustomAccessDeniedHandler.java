package com.knn.knnbank.exceptions;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.knn.knnbank.response.Response;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    
    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDenied)
            throws IOException, ServletException {
    
        Response<?> errorResponse = Response.builder()
            .statusCode(HttpStatus.FORBIDDEN.value())  // 401 Unauthorized
            .message(accessDenied.getMessage())
            .build();      
           
        response.setContentType("application/json");
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));    
    }
}
