package com.knn.knnbank.notification.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.knn.knnbank.notification.entity.Notification;

public interface NotificationRepo extends JpaRepository<Notification, Long> {
    
}
