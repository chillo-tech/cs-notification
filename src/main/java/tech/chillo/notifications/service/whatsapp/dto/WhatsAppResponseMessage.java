package tech.chillo.notifications.service.whatsapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class WhatsAppResponseMessage {
    String messaging_product;
    String id;
    List<WhatsappContact> contacts;
    List<WhatsAppMessage> messages;
}
