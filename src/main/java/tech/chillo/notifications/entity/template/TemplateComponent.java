package tech.chillo.notifications.entity.template;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.chillo.notifications.enums.TemplateComponentDataFormat;
import tech.chillo.notifications.enums.TemplateComponentType;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TemplateComponent {

    TemplateComponentType type;
    TemplateComponentDataFormat format;
    String text;
    TemplateExample example;
    List<TemplateButton> buttons;
}
