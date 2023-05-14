package tech.chillo.notifications.entity.template;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.chillo.notifications.enums.TemplateButtonType;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TemplateButton {
    TemplateButtonType type;
    String text;
    String phone_number;
    List<String> example;
}
