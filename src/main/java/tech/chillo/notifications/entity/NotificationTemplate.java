package tech.chillo.notifications.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import tech.chillo.notifications.enums.NotificationType;

@Data
@EqualsAndHashCode
@ToString(callSuper = true)
@Document("NOTIFICATION_TEMPLATE")
public class NotificationTemplate {

    @Id
    String id;
    String name;
    String application;
    int version;
    NotificationType type;
    String content;
    String injectHtmlSelector;

}
