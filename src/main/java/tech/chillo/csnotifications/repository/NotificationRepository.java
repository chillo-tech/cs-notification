package tech.chillo.csnotifications.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tech.chillo.csnotifications.entity.Notification;

public interface NotificationRepository extends MongoRepository<Notification, String> {
}
