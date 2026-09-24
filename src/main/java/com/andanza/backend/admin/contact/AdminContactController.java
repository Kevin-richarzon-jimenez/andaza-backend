package com.andanza.backend.admin.contact;

import com.andanza.backend.catalog.PageResponse;
import com.andanza.backend.common.PageParams;
import com.andanza.backend.contact.ContactMessageResponse;
import com.andanza.backend.contact.ContactService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/contact-messages")
@Tag(name = "Admin - Contact messages")
public class AdminContactController {

    private final ContactService contactService;

    public AdminContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    // Los mensajes del formulario de contacto, del más reciente al más antiguo.
    @GetMapping
    public ResponseEntity<PageResponse<ContactMessageResponse>> list(@Valid @ModelAttribute PageParams params) {
        return ResponseEntity.ok(contactService.listMessages(params));
    }
}
