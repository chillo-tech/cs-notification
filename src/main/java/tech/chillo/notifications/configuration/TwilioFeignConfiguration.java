package tech.chillo.notifications.configuration;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.form.FormEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
public class TwilioFeignConfiguration implements RequestInterceptor {
    @Value("${providers.twilio.account-id}")
    String ACCOUNT_SID;
    @Value("${providers.twilio.account-secret}")
    String AUTH_TOKEN;

    @Value("${providers.whatsapp.token}")
    String token;

    @Autowired
    private ObjectFactory<HttpMessageConverters> messageConverters;

    @Bean
    @Primary
    @Scope(SCOPE_PROTOTYPE)
    FormEncoder feignFormEncoder() {
        return new FormEncoder(new SpringEncoder(this.messageConverters));
    }

    /*
        @Bean
        public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {
            return new BasicAuthRequestInterceptor(this.ACCOUNT_SID, this.AUTH_TOKEN);
        }
    */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        log.info("Intercept request {}", requestTemplate.feignTarget().name());


        requestTemplate.header("Accept", APPLICATION_JSON_VALUE);
        if (requestTemplate.feignTarget().name().equalsIgnoreCase("whatsappmessages")) {
            requestTemplate.header("Authorization", "Bearer " + this.token);
        }
        if (requestTemplate.feignTarget().name().equalsIgnoreCase("whatsappmessages")) {
            requestTemplate.header("Authorization", "Bearer " + this.token);
        }
    }
}
