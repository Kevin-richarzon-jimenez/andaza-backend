package com.andanza.backend.contact;

import com.andanza.backend.exception.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactService {

    private final ContactMessageRepository contactMessageRepository;
    private final NewsletterSubscriptionRepository newsletterRepository;

    public ContactService(ContactMessageRepository contactMessageRepository,
                          NewsletterSubscriptionRepository newsletterRepository) {
        this.contactMessageRepository = contactMessageRepository;
        this.newsletterRepository = newsletterRepository;
    }

    // Devuelve el radicado (ticket) del mensaje guardado.
    // TODO: enviar el aviso por correo al equipo de soporte cuando exista un servicio de correo.
    @Transactional
    public String sendMessage(ContactRequest request) {
        ContactMessage message = new ContactMessage();
        message.setName(request.name().trim());
        message.setEmail(request.email().trim());
        message.setSubject(request.subject().trim());
        message.setMessage(request.message().trim());
        contactMessageRepository.save(message);
        return "TCK-" + message.getId().toString().substring(0, 8).toUpperCase();
    }

    @Transactional
    public void subscribe(NewsletterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (newsletterRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("email", "Este correo ya está suscrito al boletín");
        }
        NewsletterSubscription subscription = new NewsletterSubscription();
        subscription.setEmail(email);
        newsletterRepository.save(subscription);
    }
}
