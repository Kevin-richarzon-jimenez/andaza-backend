package com.andanza.backend.contact;

import java.time.Instant;
import java.util.UUID;

public record ContactMessageResponse(
        UUID id,
        String name,
        String email,
        String subject,
        String message,
        Instant createdAt
) {

    public static ContactMessageResponse from(ContactMessage contactMessage) {
        return new ContactMessageResponse(contactMessage.getId(), contactMessage.getName(), contactMessage.getEmail(),
                contactMessage.getSubject(), contactMessage.getMessage(), contactMessage.getCreatedAt());
    }
}
