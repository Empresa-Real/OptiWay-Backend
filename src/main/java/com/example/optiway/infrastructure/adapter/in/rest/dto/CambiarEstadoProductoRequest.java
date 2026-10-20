package com.example.optiway.infrastructure.adapter.in.rest.dto;

import com.example.optiway.application.port.in.CambiarEstadoProductoCommand;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoProductoRequest(
        @NotNull(message = "Debe indicar si el producto estará activo o inactivo")
        Boolean activo
) {

    public CambiarEstadoProductoCommand toCommand(Long id) {
        return new CambiarEstadoProductoCommand(id, activo);
    }
}
