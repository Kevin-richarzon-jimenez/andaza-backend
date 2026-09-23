package com.andanza.backend.contact;

import com.andanza.backend.common.MessageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Contact & Newsletter")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping("/contact")
    public ResponseEntity<MessageResponse> sendMessage(@Valid @RequestBody ContactRequest request) {
        String ticketId = contactService.sendMessage(request);
        return ResponseEntity.status(201).body(new MessageResponse(
                "Gracias por escribirnos. Tu radicado es " + ticketId + ", te responderemos pronto."));
    }

    @PostMapping("/newsletter")
    public ResponseEntity<MessageResponse> subscribe(@Valid @RequestBody NewsletterRequest request) {
        contactService.subscribe(request);
        return ResponseEntity.status(201).body(new MessageResponse("¡Te suscribiste correctamente al boletín!"));
    }
}
