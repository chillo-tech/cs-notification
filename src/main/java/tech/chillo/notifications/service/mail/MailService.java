package tech.chillo.notifications.service.mail;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import tech.chillo.notifications.entity.Notification;
import tech.chillo.notifications.entity.Recipient;
import tech.chillo.notifications.entity.TemplateParams;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;


@AllArgsConstructor
@Service
public class MailService {
    private final SpringTemplateEngine templateEngine;
    private final JavaMailSender mailSender;

    public void send(final Notification notification) {

        notification.getTo().parallelStream().forEach((Recipient to) -> {
            try {
                notification.setTo(Set.of(to));
                final Map<String, Object> params = defaultParameters(notification.getParams());
                params.put("firstName", to.getFirstname());
                params.put("lastName", to.getLastname());
                final Context context = new Context();
                context.setVariables(params);
                final String template = this.templateEngine.process(notification.getTemplate(), context);
                this.sendMessage(notification, template);
            } catch (final MessagingException e) {
                e.printStackTrace();
            }
        });

    }

    private void sendMessage(final Notification notification, final String template) throws MessagingException {
        final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, UTF_8.name());
        final InternetAddress[] mappedRecipients = mappedUsers(notification.getTo());
        helper.setTo(mappedRecipients);
        final InternetAddress[] mappedCC = mappedUsers(notification.getCc());
        helper.setCc(mappedCC);
        final InternetAddress[] mappedCCI = mappedUsers(notification.getCci());
        helper.setCc(mappedCCI);
        helper.setFrom(new InternetAddress(notification.getFrom().getEmail()));
        helper.setSubject(notification.getSubject());
        helper.setText(template, true);
        this.mailSender.send(mimeMessage);
    }

    private static InternetAddress[] mappedUsers(final Set<Recipient> recipients) {

        return recipients.stream().map((Recipient to) -> {
                    try {
                        final String name = String.format("%s %s", to.getFirstname(), to.getLastname().toUpperCase());
                        return new InternetAddress(to.getEmail(), name);
                    } catch (final UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .toArray(InternetAddress[]::new);
    }

    private static Map<String, Object> defaultParameters(final TemplateParams templateParams) {
        final Gson gson = new GsonBuilder().create();
        final String json = gson.toJson(templateParams);// obj
        final Map<String, Object> params = gson.fromJson(json, Map.class);

        params.put("facebookLink", "https://www.facebook.com/Chillotech-103869952427034");
        params.put("linkedinLink", "https://www.linkedin.com/company/chillo-tech");
        params.put("instagramLink", "https://www.facebook.com/Chillotech-103869952427034");
        params.put("applicationLink", "https://www.chillo.tech");

        return params;
    }
}
