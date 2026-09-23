package com.andanza.backend.admin.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Role {
    CUSTOMER("Customer"),
    ADMIN("Admin");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static Role fromLabel(String label) {
        for (Role role : values()) {
            if (role.label.equalsIgnoreCase(label)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Rol no reconocido: " + label);
    }

    @Override
    public String toString() {
        return label;
    }
}
