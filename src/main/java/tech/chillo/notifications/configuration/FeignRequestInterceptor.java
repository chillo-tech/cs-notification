package tech.chillo.notifications.configuration;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    @Value("${providers.twilio.account-id}")
    String ACCOUNT_SID;
    @Value("${providers.twilio.account-secret}")
    String AUTH_TOKEN;
    @Value("${providers.brevo.token}")
    String brevoToken;
    @Value("${providers.whatsapp.token}")
    String whatsappToken;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        log.info("Intercept request {}", requestTemplate.feignTarget().name());

        requestTemplate.header("content-type", APPLICATION_JSON_VALUE);
        requestTemplate.header("produces", APPLICATION_JSON_VALUE);
        if (requestTemplate.feignTarget().name().equalsIgnoreCase("whatsappmessages")) {
            requestTemplate.header("Authorization", "Bearer " + this.whatsappToken);
        }

        if (requestTemplate.feignTarget().name().equalsIgnoreCase("whatsapp-template-messages")) {
            requestTemplate.header("Authorization", "Bearer " + this.whatsappToken);
        }

        if (requestTemplate.feignTarget().name().equalsIgnoreCase("brevomessages")) {
            requestTemplate.header("api-key", this.brevoToken);
        }
    }
}
