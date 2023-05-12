package tech.chillo.notifications.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import tech.chillo.notifications.enums.NotificationType;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "NOTIFICATION_STATUS")
public class NotificationStatus {
    @Id
    private String id;
    private String eventId;
    private String userId;
    private String providerNotificationId;
    private String localNotificationId;
    private NotificationType channel;
    private String status;
    private String code;
    private String provider;
    private String price;
    private Instant creation;
}
