package tech.chillo.csnotifications.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tech.chillo.csnotifications.entity.Notification;
import tech.chillo.csnotifications.enums.NotificationType;
import tech.chillo.csnotifications.repository.NotificationRepository;
import tech.chillo.csnotifications.service.mail.MailService;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@Service
public class NotificationService {
    private MailService mailService;
    private NotificationRepository notificationRepository;

    public void send(final Notification notification, final List<NotificationType> types) {
        types.parallelStream().forEach(type -> {
            if (NotificationType.MAIL == type) {
                this.mailService.send(notification);
                notification.setCreation(Instant.now());
                this.notificationRepository.save(notification);
            }
        });

    }
}
