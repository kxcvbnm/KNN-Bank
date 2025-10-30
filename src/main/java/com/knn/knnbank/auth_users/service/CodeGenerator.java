package com.knn.knnbank.auth_users.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

import com.knn.knnbank.auth_users.repo.PasswordResetCodeRepo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CodeGenerator {
    
    private final PasswordResetCodeRepo passwordResetCodeRepo;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;

    public String generateUniqueCode() {
        
        String code;

        do{
            code = generateRandomCode();
        } while(passwordResetCodeRepo.findByCode(code).isPresent());

        return code;
    }

    private String generateRandomCode() {

        StringBuilder stringBuilder = new StringBuilder(CODE_LENGTH);
        SecureRandom random = new SecureRandom();

        for(int i=0; i<CODE_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            stringBuilder.append(CHARACTERS.charAt(index));
        }

        return stringBuilder.toString();
    }
}
