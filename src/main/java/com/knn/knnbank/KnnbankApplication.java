package com.knn.knnbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import lombok.RequiredArgsConstructor;

@SpringBootApplication
@EnableAsync
@RequiredArgsConstructor
public class KnnbankApplication {

	public static void main(String[] args) {
		SpringApplication.run(KnnbankApplication.class, args);
	}

	// @Bean
	// CommandLineRunner runner() {
	// 	return args -> {
	// 		NotificationDTO notificationDTO = NotificationDTO.builder()
	// 			.recipient("khunanont.ti@gmail.com")
	// 			.subject("test email")
	// 			.body("Hello welcome to knn bank")
	// 			.type(NotificationType.EMAIL)
	// 			.build();
			
	// 		notificationService.sendEmail(notificationDTO, new User());
	// 	};
	// }
}
