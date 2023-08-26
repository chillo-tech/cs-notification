package tech.chillo.notifications.entity.template;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.chillo.notifications.enums.NotificationType;
import tech.chillo.notifications.enums.TemplateCategory;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class WhatsAppTemplate {
    String name;
    String application;
    boolean allow_category_change;
    String language;
    NotificationType type;
    TemplateCategory category;
    List<TemplateComponent> components;
}
