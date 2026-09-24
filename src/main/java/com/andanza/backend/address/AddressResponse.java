package com.andanza.backend.address;

import java.util.UUID;

public record AddressResponse(
        UUID id,
        String label,
        String recipientName,
        String street,
        String city,
        String department,
        String phone,
        String postalCode,
        boolean isDefault
) {

    public static AddressResponse from(Address address) {
        return new AddressResponse(address.getId(), address.getLabel(), address.getRecipientName(),
                address.getStreet(), address.getCity(), address.getDepartment(), address.getPhone(),
                address.getPostalCode(), address.isDefaultAddress());
    }
}
