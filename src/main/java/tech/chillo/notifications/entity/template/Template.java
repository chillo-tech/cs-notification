package tech.chillo.notifications.entity.template;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import tech.chillo.notifications.enums.NotificationType;
import tech.chillo.notifications.enums.TemplateCategory;
import tech.chillo.notifications.enums.TemplateState;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document("TEMPLATE")
public class Template {
    @Id
    private String id;
    TemplateState whatsAppState;
    String name;
    String slug;
    TemplateCategory category;
    List<TemplateComponent> components;
    List<NotificationType> types;

    Map<String, Integer> whatsAppMapping;
}
