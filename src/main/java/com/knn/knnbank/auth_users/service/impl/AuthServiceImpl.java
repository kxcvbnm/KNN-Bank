package com.knn.knnbank.auth_users.service.impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knn.knnbank.auth_users.dtos.LoginRequest;
import com.knn.knnbank.auth_users.dtos.LoginResponse;
import com.knn.knnbank.auth_users.dtos.RegisterRequest;
import com.knn.knnbank.auth_users.dtos.ResetPasswordRequest;
import com.knn.knnbank.auth_users.entity.PasswordResetCode;
import com.knn.knnbank.auth_users.entity.User;
import com.knn.knnbank.auth_users.repo.PasswordResetCodeRepo;
import com.knn.knnbank.auth_users.repo.UserRepo;
import com.knn.knnbank.auth_users.service.AuthService;
import com.knn.knnbank.auth_users.service.CodeGenerator;
import com.knn.knnbank.enums.AccountType;
import com.knn.knnbank.enums.Currency;
import com.knn.knnbank.exceptions.BadRequestException;
import com.knn.knnbank.exceptions.NotFoundException;
import com.knn.knnbank.notification.dtos.NotificationDTO;
import com.knn.knnbank.notification.service.NotificationService;
import com.knn.knnbank.response.Response;
import com.knn.knnbank.role.entity.Role;
import com.knn.knnbank.role.repo.RoleRepo;
import com.knn.knnbank.security.TokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final NotificationService notificationService;
    
    private final CodeGenerator codeGenerator;
    private final PasswordResetCodeRepo passwordResetCodeRepo;

    @Value("${password.reset.link}")
    private String resetLink;


    @Override
    public Response<String> register(RegisterRequest registerRequest) {
        
        List<Role> roles;

        if(registerRequest.getRoles() == null || registerRequest.getRoles().isEmpty()) {
            Role defaultRole = roleRepo.findByName("CUSTOMER")
                .orElseThrow(() -> new NotFoundException("CUSTOMER Role not found"));
            
            roles = Collections.singletonList(defaultRole);
        } else {
            roles = registerRequest.getRoles().stream()
                .map(roleName -> roleRepo.findByName(roleName)
                    .orElseThrow(() -> new NotFoundException("Role not found" + roleName)))
                .toList();    
        }

        if(userRepo.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new BadRequestException("Email already exists");
        }

        User user = User.builder()
            .firstName(registerRequest.getFirstName())
            .lastName(registerRequest.getLastName())
            .phoneNumber(registerRequest.getPhoneNumber())
            .email(registerRequest.getEmail())
            .password(passwordEncoder.encode(registerRequest.getPassword()))
            .roles(roles)
            .active(true)
            .build();

        User savedUser = userRepo.save(user);

        // Account savedAccount = accountService.createAccount(AccountType.SAVINGS, savedUser);

        Map<String, Object> vars = new HashMap();
        vars.put("name", savedUser.getFirstName());

        NotificationDTO notificationDTO = NotificationDTO.builder()
            .recipient(savedUser.getEmail())
            .subject("Welcome to KNN Bank")
            .templateName("welcome")
            .templateVariables(vars)
            .build();

        notificationService.sendEmail(notificationDTO, savedUser);

        Map<String, Object> accountVars = new HashMap();
        accountVars.put("name", savedUser.getFirstName());
        // accountVars.put("accountNumber", savedAccount.getAccountNumber());
        accountVars.put("accountType", AccountType.SAVINGS.name());
        accountVars.put("currency", Currency.THB);

        NotificationDTO accountCreatedEmail = NotificationDTO.builder()
            .recipient(savedUser.getEmail())
            .subject("Your account has been created")
            .templateName("account-created")
            .templateVariables(accountVars)
            .build();

        notificationService.sendEmail(accountCreatedEmail, savedUser);

        return Response.<String>builder()
            .statusCode(HttpStatus.OK.value())
            .message("User registered successfully")
            // .data("Account details has been sent to: " + savedUser.getEmail() + " with account number: " + savedAccount.getAccountNumber())
            .build();
    }

    @Override
    public Response<LoginResponse> login(LoginRequest loginRequest) {
        
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        User user = userRepo.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found"));

        if(!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("Password doesn't match");
        }

        String token = tokenService.generateToken(user.getEmail());
        
        LoginResponse loginResponse = LoginResponse.builder()
            .roles(user.getRoles().stream()
                .map(Role::getName)
                .toList())
            .token(token)
            .build();

        return Response.<LoginResponse>builder()
            .statusCode(HttpStatus.OK.value())
            .message("Login Successful")
            .data(loginResponse)
            .build();
    }

    @Override
    @Transactional
    public Response<?> forgotPassword(String email) {
        
        User user = userRepo.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found"));

        passwordResetCodeRepo.deleteByUserId(user.getId());

        // generate unique code
        String code = codeGenerator.generateUniqueCode();

        PasswordResetCode passwordResetCode = PasswordResetCode.builder()
            .user(user)
            .code(code)
            .expiryDate(calculateExpiryDate())
            .used(false)
            .build();

        passwordResetCodeRepo.save(passwordResetCode);
        
        // send email
        Map<String, Object> vars = new HashMap();
        vars.put("name", user.getFirstName());
        vars.put("resetLink", resetLink + code);

        NotificationDTO notificationDTO = NotificationDTO.builder()
            .recipient(user.getEmail())
            .subject("Reset Password Code")
            .templateName("reset-password")
            .templateVariables(vars)
            .build();

        notificationService.sendEmail(notificationDTO, user);

        return Response.builder()
            .statusCode(HttpStatus.OK.value())
            .message("Password reset code has been sent to: " + user.getEmail())
            .build();
    }

    @Override
    @Transactional
    public Response<?> updatePasswordViaResetCode(ResetPasswordRequest resetPasswordRequest) {
        
        String code = resetPasswordRequest.getCode();
        String newPassword = resetPasswordRequest.getNewPassword();

        // Validate the reset code
        PasswordResetCode passwordResetCode = passwordResetCodeRepo.findByCode(code)
            .orElseThrow(() -> new BadRequestException("Invalid reset code"));

        // Check if the reset code has expired
        if(passwordResetCode.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetCodeRepo.delete(passwordResetCode);  // Delete the reset code if it has expired
            throw new BadRequestException("Reset code has expired");
        }

        // Update the user's password
        User user = passwordResetCode.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        passwordResetCodeRepo.delete(passwordResetCode);  // Delete the reset code after successful update

        // send email
        Map<String, Object> vars = new HashMap();
        vars.put("name", user.getFirstName());

        NotificationDTO confirmationEmail = NotificationDTO.builder()
            .recipient(user.getEmail())
            .subject("Password Updated Successfully")
            .templateName("password-updated")
            .templateVariables(vars)
            .build();

        notificationService.sendEmail(confirmationEmail, user);

        return Response.builder()
            .statusCode(HttpStatus.OK.value())
            .message("Password updated successfully")
            .build();
    }

    private LocalDateTime calculateExpiryDate() {

        return LocalDateTime.now().plusMinutes(15);
    }
}
