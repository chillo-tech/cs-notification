package tech.chillo.notifications.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tech.chillo.notifications.entity.NotificationStatus;

import java.util.List;

public interface NotificationStatusRepository extends MongoRepository<NotificationStatus, String> {

    NotificationStatus findFirstByProviderNotificationIdOrderByCreationDesc(String id);

    List<NotificationStatus> findByEventId(String id);
}
