package tech.chillo.notifications.web;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.chillo.notifications.entity.Notification;
import tech.chillo.notifications.enums.NotificationType;
import tech.chillo.notifications.service.NotificationService;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@AllArgsConstructor
@RestController
@RequestMapping(path = "v1/notification", produces = APPLICATION_JSON_VALUE)
public class NotificationController {
    private NotificationService notificationService;

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public void send(
            @RequestHeader(name = "X-APPLICATION-NAME", required = false) final String applicationName,
            @RequestParam final List<NotificationType> types,
            @RequestBody final Notification notification) {
        this.notificationService.send(applicationName, notification, types);
    }
}
