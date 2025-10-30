package com.knn.knnbank.auth_users.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.knn.knnbank.auth_users.dtos.UpdatePasswordRequest;
import com.knn.knnbank.auth_users.dtos.UserDTO;
import com.knn.knnbank.auth_users.entity.User;
import com.knn.knnbank.auth_users.repo.UserRepo;
import com.knn.knnbank.auth_users.service.UserService;
import com.knn.knnbank.exceptions.BadRequestException;
import com.knn.knnbank.exceptions.NotFoundException;
import com.knn.knnbank.notification.dtos.NotificationDTO;
import com.knn.knnbank.notification.service.NotificationService;
import com.knn.knnbank.response.Response;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    
    private final UserRepo userRepo;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    private final String uploadDir = "uploads/profile-picture/";

    @Override
    public User getCurrentLoggedInUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null) {
            throw new NotFoundException("User not found");
        }

        String email = authentication.getName();

        return userRepo.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public Response<UserDTO> getMyProfile() {

        User user = getCurrentLoggedInUser();
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        return Response.<UserDTO>builder()
            .statusCode(HttpStatus.OK.value())
            .message("User retrieved successfully")
            .data(userDTO)
            .build();
    }

    @Override
    public Response<Page<UserDTO>> getAllUsers(int page, int size) {

        Page<User> users = userRepo.findAll(PageRequest.of(page, size));

        Page<UserDTO> userDTOs = users.map(user -> modelMapper.map(user, UserDTO.class));

        return Response.<Page<UserDTO>>builder()
            .statusCode(HttpStatus.OK.value())
            .message("Users retrieved successfully")
            .data(userDTOs)
            .build();
    }

    @Override
    public Response<?> updatePassword(UpdatePasswordRequest updatePasswordRequest) {

        User user = getCurrentLoggedInUser();

        String oldPassword = updatePasswordRequest.getOldPassword();
        String newPassword = updatePasswordRequest.getNewPassword();

        if(oldPassword == null || newPassword == null) {
            throw new BadRequestException("Old password and new password are required");
        }
        
        if(!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Old password doesn't match");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepo.save(user);

        Map<String, Object> vars = new HashMap<>();
        vars.put("name", user.getFirstName());

        NotificationDTO notificationDTO = NotificationDTO.builder()
            .recipient(user.getEmail())
            .subject("Your Password has been changed")
            .templateName("password-change")
            .templateVariables(vars)
            .build();

        notificationService.sendEmail(notificationDTO, user);

        return Response.builder()
            .statusCode(HttpStatus.OK.value())
            .message("Password changed successfully")
            .build();
    }

    @Override
    public Response<?> uploadProfilePicture(MultipartFile file) {

        User user = getCurrentLoggedInUser();

        try {
            Path uploadPath = Paths.get(uploadDir);

            if(!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            if(user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
                Path oldFile = Paths.get(user.getProfilePictureUrl());
                
                if(Files.exists(oldFile)) {
                    Files.delete(oldFile);
                }
            }

            // Generate a unique filename for the uploaded file
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";

            if(originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            String newFileName = UUID.randomUUID() + fileExtension;
            Path filePath = uploadPath.resolve(newFileName);

            Files.copy(file.getInputStream(), filePath);

            String fileUrl = uploadDir + newFileName;

            user.setProfilePictureUrl(fileUrl);
            userRepo.save(user);

            return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Profile picture uploaded successfully")
                .data(fileUrl)
                .build();

        } catch(IOException ex) {
            throw new BadRequestException("Failed to upload profile picture");
        }
    }

}
