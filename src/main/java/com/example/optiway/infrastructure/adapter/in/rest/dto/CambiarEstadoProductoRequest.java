package com.example.optiway.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotNull;

public record CambiarEstadoProductoRequest(
        @NotNull(message = "Debe indicar si el producto estará activo o inactivo")
        Boolean activo
) {
    public Boolean getActivo() {
        return activo;
    }
}
