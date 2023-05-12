package tech.chillo.notifications.records.sendinblue;

import java.util.Set;

public record Message(
        String subject,
        String htmlContent,
        Contact sender,
        Set<Contact> to
) {
}
