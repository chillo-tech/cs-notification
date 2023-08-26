package tech.chillo.notifications.records.whatsapp;

import java.util.List;

public record WhatsappEntry(
        String id,
        List<WhatsappChange> changes
) {
}
