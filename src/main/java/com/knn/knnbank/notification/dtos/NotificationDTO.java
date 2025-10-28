package com.knn.knnbank.notification.dtos;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.knn.knnbank.enums.NotificationType;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    
    private Long id;

    private String subject;

    @NotBlank(message = "Recipient is required")
    private String recipient;

    private String body;

    private NotificationType type;

    private LocalDateTime createdAt;

    private String templateName;
    private Map<String, Object> templateVariables;
}
