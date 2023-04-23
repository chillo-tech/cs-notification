package tech.chillo.notifications.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import tech.chillo.notifications.enums.Application;
import tech.chillo.notifications.enums.NotificationType;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@Document(collection = "NOTIFICATION")
public class Notification {
    @Id
    private String id;
    private String message;
    private String subject;
    private String eventId;
    private Application application;
    private Set<NotificationType> channels;
    private String template;
    private Sender from;
    private Set<Recipient> contacts;
    private NotificationType type;
    private Set<Recipient> cc;
    private Set<Recipient> cci;
    private Map<String, Object> params;
    private Instant creation;

    public Notification() {
        this.cc = new HashSet<>();
        this.cci = new HashSet<>();
    }
}
