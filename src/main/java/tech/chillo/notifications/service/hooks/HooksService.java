package tech.chillo.notifications.service.hooks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import tech.chillo.notifications.entity.NotificationStatus;
import tech.chillo.notifications.records.whatsapp.WhatsappChangeValueStatus;
import tech.chillo.notifications.records.whatsapp.WhatsappEntry;
import tech.chillo.notifications.records.whatsapp.WhatsappNotification;
import tech.chillo.notifications.repository.NotificationStatusRepository;

import java.util.List;
import java.util.Map;

import static tech.chillo.notifications.enums.NotificationType.MAIL;
import static tech.chillo.notifications.enums.NotificationType.SMS;
import static tech.chillo.notifications.enums.NotificationType.WHATSAPP;

@Slf4j
@Service
@AllArgsConstructor
public class HooksService {
    private NotificationStatusRepository notificationStatusRepository;


    public void vonage(Map<String, Object> params) {
        log.info("vonage {}", params);
        NotificationStatus notificationStatus = getNotificationStatus(params.get("MessageSid").toString());
        String status = params.get("status").toString();
        if (!Strings.isNullOrEmpty(status)) {
            status = status.toUpperCase();
        }
        notificationStatus.setStatus(status);
        notificationStatus.setPrice(params.get("price").toString());
        notificationStatus.setCode(params.get("err-code").toString());
        notificationStatus.setProvider("VONAGE");

        notificationStatus.setId(null);
        notificationStatus.setChannel(SMS);
        this.notificationStatusRepository.save(notificationStatus);
    }

    public void whatsapp(WhatsappNotification notification) {
        ObjectMapper objectMapper = new ObjectMapper();
        log.info("whatsapp {}", objectMapper.convertValue(notification, Map.class));
        List<WhatsappEntry> entry = notification.entry();
        entry.forEach(item -> {
            item.changes().forEach(whatsappChange -> {
                List<WhatsappChangeValueStatus> statuses = whatsappChange.value().statuses();
                if (statuses != null) {
                    statuses.forEach(status -> {

                        NotificationStatus notificationStatus = getNotificationStatus(status.id());
                        String messageStatus = status.status();
                        if (!Strings.isNullOrEmpty(messageStatus)) {
                            messageStatus = messageStatus.toUpperCase();
                        }
                        notificationStatus.setStatus(messageStatus);
                        notificationStatus.setProvider("WHATSAPP");
                        notificationStatus.setRecipient(status.recipient_id());

                        notificationStatus.setId(null);
                        notificationStatus.setChannel(WHATSAPP);
                        this.notificationStatusRepository.save(notificationStatus);
                    });
                }
            });
        });
    }


    public void twilio(MultiValueMap<String, Object> params) {
        log.info("twilio params {} ", params);
        NotificationStatus notificationStatus = getNotificationStatus("" + params.get("MessageSid").toArray()[0]);
        String status = String.format("%s", params.get("MessageStatus").toArray()[0]);
        if (!Strings.isNullOrEmpty(status)) {
            status = status.toUpperCase();
        }
        notificationStatus.setStatus(status);
        notificationStatus.setStatus(status);
        notificationStatus.setProvider("TWILIO");
        notificationStatus.setChannel(SMS);

        notificationStatus.setId(null);
        this.notificationStatusRepository.save(notificationStatus);
    }

    public void brevo(Map<String, Object> params) {
        if (params.get("event").toString().equalsIgnoreCase("opened")) {
            log.info("brevo params {} ", params);
        }
        NotificationStatus notificationStatus = getNotificationStatus("" + params.get("message-id"));
        String status = String.format("%s", params.get("event")).toUpperCase();
        if (!Strings.isNullOrEmpty(status)) {
            status = status.toUpperCase();
        }
        notificationStatus.setStatus(status);
        notificationStatus.setProvider("BREVO");
        notificationStatus.setChannel(MAIL);

        notificationStatus.setId(null);
        this.notificationStatusRepository.save(notificationStatus);
    }

    private NotificationStatus getNotificationStatus(String field) {
        NotificationStatus notificationStatus = this.notificationStatusRepository.findFirstByProviderNotificationIdOrderByCreationDesc(field);
        if (notificationStatus == null) {
            notificationStatus = new NotificationStatus();
        }
        return notificationStatus;
    }
}
