package tech.chillo.notifications.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.chillo.notifications.entity.Notification;
import tech.chillo.notifications.entity.NotificationStatus;
import tech.chillo.notifications.enums.NotificationType;
import tech.chillo.notifications.repository.NotificationRepository;
import tech.chillo.notifications.repository.NotificationStatusRepository;
import tech.chillo.notifications.service.mail.MailService;
import tech.chillo.notifications.service.sms.TwilioSMSService;
import tech.chillo.notifications.service.sms.VonageSMSService;
import tech.chillo.notifications.service.whatsapp.WhatsappService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationService {
    private final MailService mailService;
    private final WhatsappService whatsappService;
    private final TwilioSMSService twilioSmsService;
    private final VonageSMSService vonageSMSService;
    private final NotificationRepository notificationRepository;
    private final NotificationStatusRepository notificationStatusRepository;

    public NotificationService(final MailService mailService, final WhatsappService whatsappService, final TwilioSMSService twilioSmsService, final VonageSMSService vonageSMSService, final NotificationRepository notificationRepository, final NotificationStatusRepository notificationStatusRepository) {
        this.mailService = mailService;
        this.whatsappService = whatsappService;
        this.vonageSMSService = vonageSMSService;
        this.twilioSmsService = twilioSmsService;
        this.notificationRepository = notificationRepository;
        this.notificationStatusRepository = notificationStatusRepository;
    }

    public void send(final String application, final Notification notification, final List<NotificationType> types) {
        types.forEach(type -> {
            try {

                final List<NotificationStatus> notificationStatusList = new ArrayList<>();
                switch (type) {
                    case MAIL, EMAIL -> {
                        log.info("Message de type {}", type);
                        final List<NotificationStatus> mailStatusList = this.mailService.send(notification);
                        notificationStatusList.addAll(mailStatusList);
                    }
                    case WHATSAPP -> {
                        log.info("Message de type {}", type);
                        final List<NotificationStatus> whatsappStatusList = this.whatsappService.send(notification);
                        notificationStatusList.addAll(whatsappStatusList);
                    }
                    case SMS -> {
                        log.info("Message de type {}", type);
                        final List<NotificationStatus> smsStatusList = this.twilioSmsService.send(notification);
                        notificationStatusList.addAll(smsStatusList);
                    }
                    default -> log.info("type {} inconnu", type);
                }
                notification.setType(type);
                notification.setCreation(Instant.now());
                notification.setApplication(application);
                final Notification saved = this.notificationRepository.save(notification);
                notificationStatusList.parallelStream().forEach(notificationStatus -> notificationStatus.setLocalNotificationId(saved.getId()));

                this.notificationStatusRepository.saveAll(notificationStatusList);
            } catch (final Exception e) {
                log.error("ERREUR LORS DE L'ENVOI d'un message", e);
                e.printStackTrace();
            }
        });

    }

    public List<NotificationStatus> statistics(final String id) {
        return this.notificationStatusRepository.findByEventId(id);
    }

    public void sendInvitation(final Map<String, Object> notificationParams) {
        final Notification notification = new Notification();
        final String application = (String) notificationParams.get("application");
        final List<NotificationType> types = ((List<String>) notificationParams.get("channels")).stream().map(NotificationType::valueOf).collect(Collectors.toList());
        types.forEach(type -> {
            try {

                final List<NotificationStatus> notificationStatusList = new ArrayList<>();
                switch (type) {
                    case WHATSAPP -> {
                        log.info("Message de type {}", type);
                        final List<NotificationStatus> whatsappStatusList = this.whatsappService.sendFromParams(notificationParams, type);
                        notificationStatusList.addAll(whatsappStatusList);
                    }
                    default -> log.info("type {} inconnu", type);
                }
                notification.setType(type);
                notification.setCreation(Instant.now());
                notification.setApplication(application);
                final Notification saved = this.notificationRepository.save(notification);
                notificationStatusList.parallelStream().forEach(notificationStatus -> notificationStatus.setLocalNotificationId(saved.getId()));

                this.notificationStatusRepository.saveAll(notificationStatusList);
            } catch (final Exception e) {
                log.info("ERREUR LORS DE L'ENVOI d'un message");
                log.error("ERREUR LORS DE L'ENVOI d'un message", e);
                e.printStackTrace();
            }
        });
    }
}
