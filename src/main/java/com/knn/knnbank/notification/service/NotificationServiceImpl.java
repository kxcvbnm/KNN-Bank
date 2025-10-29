package com.knn.knnbank.notification.service;

import java.nio.charset.StandardCharsets;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.knn.knnbank.auth_users.entity.User;
import com.knn.knnbank.notification.dtos.NotificationDTO;
import com.knn.knnbank.notification.repo.NotificationRepo;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationRepo notificationRepo;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    @Async
    public void sendEmail(NotificationDTO notificationDTO, User user) {
        
        try{
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name()
            );
            helper.setTo(notificationDTO.getRecipient());
            helper.setSubject(notificationDTO.getSubject());

            // use template
            if(notificationDTO.getTemplateName() != null) {
                Context context = new Context();
                context.setVariables(notificationDTO.getTemplateVariables());
                String htmlContent = templateEngine.process(notificationDTO.getTemplateName(), context);
                helper.setText(htmlContent, true);
            } else {
                helper.setText(notificationDTO.getBody(), true); // if no template send plain text
            }
            mailSender.send(message);
            log.info("Email sent successfully");

            // save to db
            // Notification notification = Notification.builder()
            //     .recipient(notificationDTO.getRecipient())
            //     .subject(notificationDTO.getSubject())
            //     .body(notificationDTO.getBody())
            //     .type(NotificationType.EMAIL)
            //     .user(user)
            //     .build();
            
            // notificationRepo.save(notification);

        } catch(MessagingException ex) {
            log.error(ex.getMessage());
        }
    }
}
