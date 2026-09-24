package com.andanza.backend.address;

import com.andanza.backend.exception.NotFoundException;
import com.andanza.backend.user.User;
import com.andanza.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressService(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> list(UUID userId) {
        return addressRepository.findByUserIdOrderByCreatedAtAsc(userId).stream().map(AddressResponse::from).toList();
    }

    @Transactional
    public AddressResponse create(UUID userId, AddressRequest request) {
        boolean makeDefault = Boolean.TRUE.equals(request.isDefault()) || !addressRepository.existsByUserId(userId);
        if (makeDefault) {
            addressRepository.clearDefault(userId);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user", "El usuario no existe"));
        Address address = new Address();
        address.setUser(user);
        address.setDefaultAddress(makeDefault);
        apply(address, request);
        return AddressResponse.from(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse update(UUID userId, UUID id, AddressRequest request) {
        boolean makeDefault = Boolean.TRUE.equals(request.isDefault());
        if (makeDefault) {
            addressRepository.clearDefault(userId);
        }
        Address address = findOwned(userId, id);
        if (makeDefault) {
            address.setDefaultAddress(true);
        }
        apply(address, request);
        return AddressResponse.from(address);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Address address = findOwned(userId, id);
        addressRepository.delete(address);
        addressRepository.flush();
        // Si se borró la predeterminada, la dirección más antigua que quede pasa a serlo.
        if (address.isDefaultAddress()) {
            addressRepository.findByUserIdOrderByCreatedAtAsc(userId).stream().findFirst()
                    .ifPresent(next -> next.setDefaultAddress(true));
        }
    }

    // Una dirección ajena responde igual que una inexistente: no se revela que existe.
    private Address findOwned(UUID userId, UUID id) {
        return addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("id", "La dirección indicada no existe"));
    }

    private void apply(Address address, AddressRequest request) {
        address.setLabel(request.label().trim());
        address.setRecipientName(request.recipientName().trim());
        address.setStreet(request.street().trim());
        address.setCity(request.city().trim());
        address.setDepartment(request.department().trim());
        address.setPhone(request.phone().trim());
        address.setPostalCode(request.postalCode() == null || request.postalCode().isBlank() ? null : request.postalCode());
    }
}
