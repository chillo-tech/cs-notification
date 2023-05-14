package tech.chillo.notifications.service.template;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tech.chillo.notifications.entity.template.Template;
import tech.chillo.notifications.entity.template.TemplateComponent;
import tech.chillo.notifications.repository.TemplateRepository;
import tech.chillo.notifications.service.shared.SharedService;
import tech.chillo.notifications.service.whatsapp.WhatsappService;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static tech.chillo.notifications.enums.NotificationType.WHATSAPP;
import static tech.chillo.notifications.enums.TemplateComponentType.BODY;

@AllArgsConstructor
@Service
public class TemplateService {
    private final WhatsappService whatsappService;
    private final SharedService sharedService;
    private final TemplateRepository templateRepository;

    public void create(Template template) {
        String name = template.getName();
        name = String.format("ze_%s", name);
        String slug = this.sharedService.toSlug(name.toLowerCase());
        template.setSlug(slug);
        template.setName(name);

        Template templateInBDD = this.templateRepository.findBySlug(slug);

        if (templateInBDD == null) {
            TemplateComponent body = template.getComponents().stream().filter(templateComponent -> templateComponent.getType().equals(BODY)).findFirst().orElse(null);
            if (body != null && !Strings.isNullOrEmpty(body.getText())) {
                Map<String, Integer> mappings = getMatchers(body.getText());
                template.setWhatsAppMapping(mappings);
            }

            template.setName(name.toLowerCase().replaceAll(" ", "_"));
            templateInBDD = this.templateRepository.save(template);
        }
        if (template.getTypes() != null && template.getTypes().contains(WHATSAPP)) {
            this.whatsappService.createTemplate(templateInBDD);
        }
    }

    private Map<String, Integer> getMatchers(String text) {
        Map<String, Integer> mappers = new HashMap<String, Integer>();
        Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(.*?)\\}\\}");
        Matcher matcher = VARIABLE_PATTERN.matcher(text);
        int i = 1;
        while (matcher.find()) {
            String item = matcher.group();
            mappers.put(
                    item.replaceAll("\\{", "").replaceAll("\\}", ""),
                    i
            );
            i++;
        }
        return mappers;
    }


}
