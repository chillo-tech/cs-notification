package tech.chillo.notifications.service.sms;

import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import tech.chillo.notifications.configuration.TwilioFeignConfiguration;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@FeignClient(
        name = "FeignNotifications",
        url = "${providers.twilio.host}/${providers.twilio.account-id}",
        configuration = TwilioFeignConfiguration.class
)
public interface TwilioFeignCient {
    @PostMapping(value = "/Message.json", consumes = APPLICATION_FORM_URLENCODED_VALUE)
    @Headers("Content-Type: application/x-www-form-urlencoded")
    void message(Map<String, String> formParams);
}
