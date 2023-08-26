package tech.chillo.notifications.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tech.chillo.notifications.entity.Notification;

public interface NotificationRepository extends MongoRepository<Notification, String> {
}
