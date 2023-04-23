package tech.chillo.notifications.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tech.chillo.notifications.entity.NotificationTemplate;
import tech.chillo.notifications.enums.Application;
import tech.chillo.notifications.repository.NotificationTemplateRepository;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
@AllArgsConstructor
public class NotificationTemplateService {
    private NotificationTemplateRepository notificationTemplateRepository;

    private void checkTemplates(Application application, Set<NotificationTemplate> templates) {
        templates.forEach(notificationTemplate -> {
            Optional<NotificationTemplate> template = this.notificationTemplateRepository
                    .findByApplicationAndNameAndVersionAndType(
                            application,
                            notificationTemplate.getName(),
                            notificationTemplate.getVersion(),
                            notificationTemplate.getType()

                    );
            if (template.isPresent()) {
                throw new IllegalArgumentException(
                        format("un template existe déjà avec les paramètres application %s name %s version %s type %s", application,
                                notificationTemplate.getName(),
                                notificationTemplate.getVersion(),
                                notificationTemplate.getType()));
            }
        });
    }

    public void create(Application application, Set<NotificationTemplate> templates) {
        this.checkTemplates(application, templates);
        Set<String> templateNames = templates.stream().map(NotificationTemplate::getName).collect(Collectors.toSet());
        templates.forEach(template -> template.setApplication(application));
        this.notificationTemplateRepository.saveAll(templates);

    }

}
