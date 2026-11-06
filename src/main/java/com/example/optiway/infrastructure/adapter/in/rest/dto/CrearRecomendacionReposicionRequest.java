package com.example.optiway.infrastructure.adapter.in.rest.dto;

import com.example.optiway.application.port.in.CrearRecomendacionReposicionCommand;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CrearRecomendacionReposicionRequest(

        @NotNull(message = "El ID del producto es obligatorio.")
        @Positive(message = "El ID del producto debe ser positivo.")
        Long productoId,

        @NotNull(message = "La cantidad es obligatoria.")
        @Positive(message = "La cantidad debe ser mayor que cero.")
        Integer cantidad,

        @NotNull(message = "El ID del Centro de Distribución de origen es obligatorio.")
        @Positive(message = "El ID del Centro de Distribución debe ser positivo.")
        Long centroDistribucionOrigenId,

        @NotNull(message = "El ID de la tienda de destino es obligatorio.")
        @Positive(message = "El ID de la tienda debe ser positivo.")
        Long tiendaDestinoId,

        Boolean confirmarFueraDeCobertura

) {
    public CrearRecomendacionReposicionCommand toCommand() {
        return new CrearRecomendacionReposicionCommand(
                productoId,
                cantidad,
                centroDistribucionOrigenId,
                tiendaDestinoId,
                confirmarFueraDeCobertura
        );
    }
}