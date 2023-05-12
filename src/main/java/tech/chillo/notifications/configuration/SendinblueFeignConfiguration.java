package tech.chillo.notifications.configuration;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class SendinblueFeignConfiguration implements RequestInterceptor {

    private final String token;

    public SendinblueFeignConfiguration(
            @Value("${providers.sendinblue.token}") final String token
    ) {
        this.token = token;
    }

    @Override
    public void apply(final RequestTemplate requestTemplate) {
        requestTemplate.header("content-type", APPLICATION_JSON_VALUE);
        requestTemplate.header("Accept", APPLICATION_JSON_VALUE);
        //requestTemplate.header("api-key", this.token);
    }

    //@Override
    public Exception decode(String methodKey, Response response) {
        String requestUrl = response.request().url();
        Response.Body responseBody = response.body();
        HttpStatus responseStatus = HttpStatus.valueOf(response.status());

        if (responseStatus.is5xxServerError()) {
            //return new RestApiServerException(requestUrl, responseBody);
        } else if (responseStatus.is4xxClientError()) {
            return null;
        } else {
            return new Exception("Generic exception");
        }
        return null;
    }
}
