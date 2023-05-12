package tech.chillo.notifications.service.mail;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import tech.chillo.notifications.configuration.SendinblueFeignConfiguration;
import tech.chillo.notifications.records.sendinblue.Message;

import java.util.Map;

@FeignClient(name = "sendinbluemessages", url = "${providers.sendinblue.host}", configuration = SendinblueFeignConfiguration.class)
public interface SendinblueMessageService {
    @PostMapping(path = "/${providers.sendinblue.path}")
    Map<String, Object> message(@RequestBody Message message);
}
