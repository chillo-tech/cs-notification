package tech.chillo.notifications.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tech.chillo.notifications.entity.template.Template;

public interface TemplateRepository extends MongoRepository<Template, String> {
    Template findBySlug(String name);

    Template findByName(String name);
}
