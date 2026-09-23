package com.andanza.backend.admin.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AccountStatus {
    ACTIVE("Active"),
    BLOCKED("Blocked");

    private final String label;

    AccountStatus(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static AccountStatus fromLabel(String label) {
        for (AccountStatus status : values()) {
            if (status.label.equalsIgnoreCase(label)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Estado no reconocido: " + label);
    }

    @Override
    public String toString() {
        return label;
    }
}
