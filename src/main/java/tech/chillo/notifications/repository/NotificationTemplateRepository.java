package tech.chillo.notifications.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tech.chillo.notifications.entity.NotificationTemplate;
import tech.chillo.notifications.enums.NotificationType;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends MongoRepository<NotificationTemplate, String> {

    Optional<NotificationTemplate> findByApplicationAndName(String application, String name);

    Optional<NotificationTemplate> findByApplicationAndNameAndVersionAndType(String application, String name, int version, NotificationType type);

    List<NotificationTemplate> findByApplicationAndNameIn(String application, Iterable<String> names);

    List<NotificationTemplate> findByApplication(String application);

    NotificationTemplate findByName(String templateName);
}
