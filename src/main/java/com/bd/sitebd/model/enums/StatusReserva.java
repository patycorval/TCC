
package com.bd.sitebd.model.enums;

public enum StatusReserva {
    PENDENTE("Pendente"),
    APROVADA("Aprovada"),
    REJEITADA("Rejeitada");

    private final String displayName;

    StatusReserva(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}