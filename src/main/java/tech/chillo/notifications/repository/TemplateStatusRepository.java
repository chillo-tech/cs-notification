package tech.chillo.notifications.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tech.chillo.notifications.entity.TemplateStatus;

public interface TemplateStatusRepository extends MongoRepository<TemplateStatus, String> {

    TemplateStatus findFirstByProviderTemplateIdOrderByCreationDesc(String id);
}
