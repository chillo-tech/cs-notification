package tech.chillo.notifications.service.hooks;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import tech.chillo.notifications.entity.NotificationStatus;
import tech.chillo.notifications.repository.NotificationStatusRepository;

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
        notificationStatus.setStatus(params.get("status").toString());
        notificationStatus.setPrice(params.get("price").toString());
        notificationStatus.setCode(params.get("err-code").toString());
        notificationStatus.setProvider("VONAGE");

        notificationStatus.setId(null);
        notificationStatus.setChannel(SMS);
        this.notificationStatusRepository.save(notificationStatus);
    }

    public void whatsapp(Map<String, Object> params) {
        log.info("whatsapp {}", params);
        NotificationStatus notificationStatus = getNotificationStatus(params.get("MessageSid").toString());
        notificationStatus.setProvider("WHATSAPP");
        notificationStatus.setChannel(WHATSAPP);
    }


    public void twilio(MultiValueMap<String, Object> params) {
        log.info("twilio params {} ", params);
        NotificationStatus notificationStatus = getNotificationStatus("" + params.get("MessageSid").toArray()[0]);
        notificationStatus.setStatus(String.format("%s", params.get("MessageStatus").toArray()[0]).toUpperCase());
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
        notificationStatus.setStatus(String.format("%s", params.get("event")).toUpperCase());
        notificationStatus.setProvider("SENDINBLUE");
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
