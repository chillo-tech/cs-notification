package tech.chillo.notifications.service.whatsapp;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import tech.chillo.notifications.entity.template.WhatsAppTemplate;
import tech.chillo.notifications.service.whatsapp.dto.WhatsAppResponse;

@FeignClient(name = "whatsapp-template-messages", url = "${providers.whatsapp.template-host}")
public interface TemplateMessageService {

    @PostMapping("/message_templates")
    WhatsAppResponse template(@RequestBody WhatsAppTemplate whatsAppTemplate);

}
