package com.andanza.backend.address;

import org.springframework.stereotype.Service;

import java.util.UUID;

// Sin BD: valida y simula la creación con un id nuevo; no se guarda
// ninguna lista real de direcciones por usuario.
@Service
public class AddressService {

    public AddressResponse create(AddressRequest request) {
        // TODO (BD): asociar y guardar la dirección al usuario autenticado.
        String id = "d-" + UUID.randomUUID().toString().substring(0, 8);
        return toResponse(id, request);
    }

    public AddressResponse update(String id, AddressRequest request) {
        // TODO (BD): validar que la dirección pertenezca al usuario y actualizarla.
        return toResponse(id, request);
    }

    private AddressResponse toResponse(String id, AddressRequest request) {
        return new AddressResponse(id, request.label(), request.city(), request.address(),
                request.phone(), request.postalCode());
    }
}
