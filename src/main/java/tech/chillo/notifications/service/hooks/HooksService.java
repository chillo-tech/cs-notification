package tech.chillo.notifications.service.hooks;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.chillo.notifications.entity.NotificationStatus;
import tech.chillo.notifications.repository.NotificationStatusRepository;

import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class HooksService {
    private NotificationStatusRepository notificationStatusRepository;


    public void vonage(Map<String, Object> params) {
        log.info("{}", params);
        NotificationStatus notificationStatus = this.notificationStatusRepository.findByProviderNotificationId(params.get("messageId").toString());
        if (notificationStatus == null) {
            notificationStatus = new NotificationStatus();
        }
        notificationStatus.setStatus(params.get("status").toString());
        notificationStatus.setPrice(params.get("price").toString());
        notificationStatus.setCode(params.get("err-code").toString());
        notificationStatus.setProvider("VONAGE");

        notificationStatus.setId(null);
        this.notificationStatusRepository.save(notificationStatus);
    }

    public void whatsapp(Map<String, Object> params) {
        log.info("{}", params);
    }
}
