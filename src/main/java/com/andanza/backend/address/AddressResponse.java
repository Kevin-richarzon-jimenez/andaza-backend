package com.andanza.backend.address;

public record AddressResponse(
        String id,
        String label,
        String city,
        String address,
        String phone,
        String postalCode
) {
}
