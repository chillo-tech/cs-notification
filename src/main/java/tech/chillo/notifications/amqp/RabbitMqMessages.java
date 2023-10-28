package tech.chillo.notifications.amqp;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tech.chillo.notifications.entity.Notification;
import tech.chillo.notifications.service.NotificationService;

@AllArgsConstructor
@Slf4j
@Component
public class RabbitMqMessages {
    private NotificationService notificationService;

    @RabbitListener(
            queues = {"${application.messages.send.queue}"},
            returnExceptions = "rabbitErrorHandler",
            errorHandler = "rabbitErrorHandler"
    )
    public void handleMessage(final Notification notification) {
        log.info("Envoi du message {} {} sur les cannaux {}", notification.getEventId(), notification.getSubject(), notification.getChannels());
        //this.notificationService.send(notification.getApplication(), notification, notification.getChannels().stream().toList());
    }

}
