package com.example.optiway.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AjustarInventarioCDRequest(
        @NotNull(message = "centroDistribucionId es obligatorio") Long centroDistribucionId,
        @NotNull(message = "productoId es obligatorio") Long productoId,
        @NotNull(message = "nuevaCantidad es obligatoria") @Min(value = 0, message = "nuevaCantidad debe ser mayor o igual a cero") Integer nuevaCantidad) {
}
