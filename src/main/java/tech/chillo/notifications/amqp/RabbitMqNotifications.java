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
public class RabbitMqNotifications {
    private NotificationService notificationService;

    @RabbitListener(
            queues = "${application.notifications.queue}",
            returnExceptions = "rabbitErrorHandler",
            errorHandler = "rabbitErrorHandler"
    )
    public void handleMessage(final String message) {
        log.info("Traitement du message {}", message);
    }

    @RabbitListener(
            queues = "${application.messages.queue}",
            returnExceptions = "rabbitErrorHandler",
            errorHandler = "rabbitErrorHandler"
    )
    public void handleMessage(final Notification message) {
        log.info("Traitement de la notification {} {}", message.getEventId(), message.getSubject());
        this.notificationService.send(message.getApplication(), message, message.getChannels().stream().toList());
        /*
        message.getChannels().forEach(channel -> {
            switch (channel) {
                case EMAIL -> {
                    this.mailService.send(message);
                }
                case SMS -> this.twilioSMSService.send(message);
                //case WHATSAPP -> this.whatsappService.send(message);
            }
        });

         */
    }
}
