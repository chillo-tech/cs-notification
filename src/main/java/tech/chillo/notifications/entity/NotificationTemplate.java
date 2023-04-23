package tech.chillo.notifications.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import tech.chillo.notifications.enums.Application;
import tech.chillo.notifications.enums.NotificationType;

@Data
@EqualsAndHashCode
@ToString(callSuper = true)
@Document("NOTIFICATION_TEMPLATE")
public class NotificationTemplate {

    @Id
    @JsonIgnore
    String id;
    String name;
    Application application;
    int version;
    NotificationType type;
    String content;
    String injectHtmlSelector;

}
