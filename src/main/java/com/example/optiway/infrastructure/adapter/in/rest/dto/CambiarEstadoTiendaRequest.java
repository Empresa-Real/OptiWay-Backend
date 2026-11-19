package com.example.optiway.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotNull;

public class CambiarEstadoTiendaRequest {

    @NotNull(message = "El campo activo es obligatorio")
    private Boolean activo;

    private Boolean confirmarConOrdenesEnTransito;

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Boolean getConfirmarConOrdenesEnTransito() {
        return confirmarConOrdenesEnTransito;
    }

    public void setConfirmarConOrdenesEnTransito(Boolean confirmarConOrdenesEnTransito) {
        this.confirmarConOrdenesEnTransito = confirmarConOrdenesEnTransito;
    }
}