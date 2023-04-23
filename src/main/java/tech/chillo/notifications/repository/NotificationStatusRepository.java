package tech.chillo.notifications.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tech.chillo.notifications.entity.NotificationStatus;

public interface NotificationStatusRepository extends MongoRepository<NotificationStatus, String> {

    NotificationStatus findByProviderNotificationId(String id);
}
