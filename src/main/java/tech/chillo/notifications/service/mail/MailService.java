package tech.chillo.notifications.service.mail;

import lombok.extern.slf4j.Slf4j;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tech.chillo.notifications.entity.Notification;
import tech.chillo.notifications.entity.NotificationStatus;
import tech.chillo.notifications.entity.Recipient;
import tech.chillo.notifications.records.brevo.Contact;
import tech.chillo.notifications.records.brevo.Message;
import tech.chillo.notifications.repository.NotificationTemplateRepository;
import tech.chillo.notifications.service.NotificationMapper;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static tech.chillo.notifications.data.ApplicationData.FOOTER_TEXT;
import static tech.chillo.notifications.enums.NotificationType.MAIL;

@Slf4j
@Service
public class MailService extends NotificationMapper {
    private final JavaMailSender mailSender;
    private final String recipient;
    private final SendinblueMessageService brevoMessageService;

    public MailService(
            final NotificationTemplateRepository notificationTemplateRepository,
            final JavaMailSender mailSender,
            final SendinblueMessageService brevoMessageService,
            @Value("${application.recipient.email:#{null}}") final String recipient) {
        super(notificationTemplateRepository);
        this.brevoMessageService = brevoMessageService;
        this.mailSender = mailSender;
        this.recipient = recipient;
    }

    @Async
    public List<NotificationStatus> send(final Notification notification) {
        return notification.getContacts().stream().map((Recipient to) -> {
            String messageToSend = String.valueOf(this.map(notification, to).get("message"));

            try {
                Map<String, Object> result = this.sendMessageUsingSendinBlueAPI(notification, messageToSend, to);

                NotificationStatus notificationStatus = this.getNotificationStatus(
                        notification,
                        to.getId(),
                        MAIL,
                        result.get("messageId").toString(),
                        "INITIAL"
                );
                notificationStatus.setProvider("BREVO");
                return notificationStatus;
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
    }


    private Map<String, Object> sendMessageUsingSendinBlueAPI(final Notification notification, final String messageToSend, Recipient to) throws MessagingException {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(String.format("%s<p>%s</p>", messageToSend, FOOTER_TEXT));
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        String lastName = notification.getFrom().getLastName();
        if (lastName != null) {
            lastName = lastName.toUpperCase();
        }

        String firstName = notification.getFrom().getFirstName();
        if (firstName != null) {
            firstName = format("%s%s", firstName.substring(0, 1).toUpperCase(), firstName.substring(1).toLowerCase());
        }

        Message message = new Message(
                notification.getSubject(),
                renderer.render(document),
                new Contact(format("%s %s VIA ZEEVEN", firstName, lastName), notification.getFrom().getEmail()),
                this.mappedContacts(Set.of(to))
        );
        return this.brevoMessageService.message(message);
    }

    private void sendMessage(final Notification notification, final String template) throws MessagingException {
        final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
        final InternetAddress[] mappedRecipients = this.mappedUsers(notification.getContacts());
        helper.setTo(mappedRecipients);
        final InternetAddress[] mappedCC = this.mappedUsers(notification.getCc());
        helper.setCc(mappedCC);
        final InternetAddress[] mappedCCI = this.mappedUsers(notification.getCci());
        helper.setCc(mappedCCI);
        final InternetAddress from = this.getInternetAddress(notification.getFrom().getFirstName(), notification.getFrom().getLastName(), notification.getFrom().getEmail());
        helper.setFrom(Objects.requireNonNull(from));
        helper.setSubject(notification.getSubject());
        helper.setText(template, true);
        this.mailSender.send(mimeMessage);
    }

    private Set<Contact> mappedContacts(final Set<Recipient> recipients) {

        return recipients.stream().map(
                        (Recipient to) -> {
                            String email = this.recipient;
                            if (this.recipient == null) {
                                email = to.getEmail();
                            }
                            return new Contact(format("%s %s", to.getFirstName(), to.getLastName()), email);
                        })
                .collect(Collectors.toSet());
    }

    private InternetAddress[] mappedUsers(final Set<Recipient> recipients) {

        return recipients.stream().map(
                        (Recipient to) -> {
                            String email = this.recipient;
                            if (this.recipient == null) {
                                email = to.getEmail();
                            }
                            return this.getInternetAddress(to.getFirstName(), to.getLastName(), email);
                        })
                .toArray(InternetAddress[]::new);
    }

    private InternetAddress getInternetAddress(final String firstname, final String lastname, final String email) {
        try {
            final String name = format("%s %s", firstname, lastname);
            return new InternetAddress(email, name);
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }


}
