package com.example.optiway.application.port.in.dto;

public class OrdenesEnTransitoResponse {

    private boolean tieneOrdenesEnTransito;

    public OrdenesEnTransitoResponse(boolean tieneOrdenesEnTransito) {
        this.tieneOrdenesEnTransito = tieneOrdenesEnTransito;
    }

    public boolean isTieneOrdenesEnTransito() {
        return tieneOrdenesEnTransito;
    }

    public void setTieneOrdenesEnTransito(boolean tieneOrdenesEnTransito) {
        this.tieneOrdenesEnTransito = tieneOrdenesEnTransito;
    }
}