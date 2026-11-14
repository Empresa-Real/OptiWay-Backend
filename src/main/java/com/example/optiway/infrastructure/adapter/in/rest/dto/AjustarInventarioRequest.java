package com.example.optiway.infrastructure.adapter.in.rest.dto;

import com.example.optiway.application.port.in.AjustarInventarioCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AjustarInventarioRequest(
        @Positive(message = "El ID de la tienda debe ser positivo")
        Long tiendaId,

        @Positive(message = "El ID del centro de distribución debe ser positivo")
        Long centroDistribucionId,

        @NotNull(message = "El ID del producto es obligatorio")
        @Positive(message = "El ID del producto debe ser positivo")
        Long productoId,

        @NotNull(message = "La nueva cantidad es obligatoria")
        @Min(value = 0, message = "La nueva cantidad debe ser mayor o igual a cero")
        Integer nuevaCantidad
) {
    public AjustarInventarioCommand toCommand() {
        return new AjustarInventarioCommand(tiendaId, centroDistribucionId, productoId, nuevaCantidad);
    }
}
