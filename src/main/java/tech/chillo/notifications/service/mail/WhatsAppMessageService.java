package tech.chillo.notifications.service.mail;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import tech.chillo.notifications.service.whatsapp.dto.WhatsAppResponse;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "whatsappendpoint", url = "${providers.whatsapp.host}")
public interface WhatsAppMessageService {

    @PostMapping(path = "messages", produces = APPLICATION_JSON_VALUE)
    WhatsAppResponse message(@RequestBody Map<String, Object> message);
}
