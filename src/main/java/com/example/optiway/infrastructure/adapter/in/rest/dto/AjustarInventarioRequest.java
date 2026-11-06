package com.example.optiway.infrastructure.adapter.in.rest.dto;

import com.example.optiway.application.port.in.AjustarInventarioCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AjustarInventarioRequest(
        @NotNull(message = "El ID de la tienda es obligatorio")
        @Positive(message = "El ID de la tienda debe ser positivo")
        Long tiendaId,

        @NotNull(message = "El ID del producto es obligatorio")
        @Positive(message = "El ID del producto debe ser positivo")
        Long productoId,

        @NotNull(message = "La diferencia de inventario es obligatoria")
        Integer diferencia
) {
    public AjustarInventarioCommand toCommand() {
        return new AjustarInventarioCommand(tiendaId, productoId, diferencia);
    }
}
