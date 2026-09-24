package com.andanza.backend.address;

import com.andanza.backend.auth.CurrentUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/account/addresses")
@Tag(name = "Account - Addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> list(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(addressService.list(CurrentUser.id(jwt)));
    }

    @PostMapping
    public ResponseEntity<AddressResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                  @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(201).body(addressService.create(CurrentUser.id(jwt), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> update(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                                  @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.update(CurrentUser.id(jwt), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        addressService.delete(CurrentUser.id(jwt), id);
        return ResponseEntity.noContent().build();
    }
}
