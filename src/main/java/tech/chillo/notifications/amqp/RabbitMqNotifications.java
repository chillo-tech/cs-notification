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

    /*
        @RabbitListener(
                queues = "${application.notifications.queue}",
                returnExceptions = "rabbitErrorHandler",
                errorHandler = "rabbitErrorHandler"
        )
        public void handleMessage(final String message) {
            log.info("Traitement du message {}", message);
        }
    */
    @RabbitListener(
            queues = "${application.messages.queue}",
            returnExceptions = "rabbitErrorHandler",
            errorHandler = "rabbitErrorHandler"
    )
    public void handleMessage(final Notification notification) {
        /*
        log.info("Traitement du message {}", message);
        final Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .registerTypeAdapter(LocalDate.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(LocalDate.class, new LocalDateTimeTypeAdapter())
                .create();

        final Notification notification = gson.fromJson(message, Notification.class);
        */
        log.info("Traitement de la notification {} {}", notification.getEventId(), notification.getSubject());
        this.notificationService.send(notification.getApplication(), notification, notification.getChannels().stream().toList());
    }
}
