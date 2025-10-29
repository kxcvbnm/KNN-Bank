package com.knn.knnbank.notification.service;

import com.knn.knnbank.auth_users.entity.User;
import com.knn.knnbank.notification.dtos.NotificationDTO;

public interface NotificationService {
    
    void sendEmail(NotificationDTO notificationDTO, User user);
}
