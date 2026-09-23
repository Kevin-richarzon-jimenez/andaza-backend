package com.andanza.backend.contact;

import com.andanza.backend.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Sin BD: no se envía correo real ni se guarda el ticket/suscripción;
// solo se valida y se simula la respuesta.
@Service
public class ContactService {

    // Set en memoria SOLO para poder demostrar la regla de "no duplicados"
    // dentro de una misma ejecución. No es persistencia real: se reinicia
    // cada vez que la aplicación se reinicia. ConcurrentHashMap.newKeySet()
    // porque este service es un singleton compartido entre requests
    // concurrentes -- un HashSet normal no es seguro ahí.
    // TODO (BD): reemplazar esto por una tabla de suscriptores.
    private final Set<String> sessionSubscribers = ConcurrentHashMap.newKeySet();

    public String sendMessage(ContactRequest request) {
        // TODO (BD / notificaciones): enviar el mensaje real (correo, ticket de soporte, etc.)
        return "TCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public void subscribe(NewsletterRequest request) {
        String email = request.email().toLowerCase();
        // add() ya es atómico: si devuelve false es porque ya estaba.
        // TODO (BD): persistir el correo en la tabla de suscriptores.
        if (!sessionSubscribers.add(email)) {
            throw new BusinessException("email", "Este correo ya está suscrito al boletín");
        }
    }
}
