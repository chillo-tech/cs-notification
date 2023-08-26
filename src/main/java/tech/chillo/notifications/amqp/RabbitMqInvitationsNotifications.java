package tech.chillo.notifications.amqp;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tech.chillo.notifications.service.NotificationService;

import java.util.Map;

@AllArgsConstructor
@Slf4j
@Component
public class RabbitMqInvitationsNotifications {
    private NotificationService notificationService;

    @RabbitListener(
            queues = {"${application.invitations.queue}"},
            returnExceptions = "rabbitErrorHandler",
            errorHandler = "rabbitErrorHandler"
    )
    public void handleMessage(final Map<String, Object> notification) {
        log.info("Traitement de l'invitation");
        this.notificationService.sendInvitation(notification);
    }

}
