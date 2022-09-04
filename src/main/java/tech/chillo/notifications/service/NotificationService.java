package tech.chillo.notifications.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tech.chillo.notifications.entity.Notification;
import tech.chillo.notifications.enums.NotificationType;
import tech.chillo.notifications.repository.NotificationRepository;
import tech.chillo.notifications.service.mail.MailService;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@Service
public class NotificationService {
    private MailService mailService;
    private NotificationRepository notificationRepository;

    public void send(String applicationName, final Notification notification, final List<NotificationType> types) {
        types.parallelStream().forEach(type -> {
            if (NotificationType.MAIL == type) {
                this.mailService.send(notification);
                notification.setCreation(Instant.now());
                notification.setApplicationName(applicationName);
                this.notificationRepository.save(notification);
            }
        });

    }
}
