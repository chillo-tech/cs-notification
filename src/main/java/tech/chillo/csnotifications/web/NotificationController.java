package tech.chillo.csnotifications.web;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.chillo.csnotifications.entity.Notification;
import tech.chillo.csnotifications.enums.NotificationType;
import tech.chillo.csnotifications.service.NotificationService;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@AllArgsConstructor
@RestController
@RequestMapping(path = "v1/notification", produces = APPLICATION_JSON_VALUE)
public class NotificationController {
    private NotificationService notificationService;

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public void send(
            @RequestHeader("X-APPLICATION_NAME") final String applicationName,
            @RequestParam final List<NotificationType> types,
            @RequestBody final Notification notification) {
        this.notificationService.send(notification, types);
    }
}
