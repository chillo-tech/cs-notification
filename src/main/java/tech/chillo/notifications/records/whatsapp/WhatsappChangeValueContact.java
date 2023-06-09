package tech.chillo.notifications.records.whatsapp;


record WhatsappChangeValueContactProfile(
        String name
) {

}

public record WhatsappChangeValueContact(
        String wa_id,
        WhatsappChangeValueContactProfile profile
) {
}
