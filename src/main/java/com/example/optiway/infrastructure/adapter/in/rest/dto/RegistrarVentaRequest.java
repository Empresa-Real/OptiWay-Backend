package com.example.optiway.infrastructure.adapter.in.rest.dto;

import com.example.optiway.application.port.in.RegistrarVentaCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

@Schema(description = "Petición para registrar una venta en una tienda (el id de la tienda viaja en la URL)")
public record RegistrarVentaRequest(
        @NotNull(message = "El ID del producto es obligatorio")
        @Positive(message = "El ID del producto debe ser positivo")
        @Schema(description = "Identificador del producto vendido", example = "5")
        Long productoId,

        @NotNull(message = "La cantidad vendida es obligatoria")
        @Positive(message = "La cantidad vendida debe ser mayor a cero")
        @Schema(description = "Cantidad de unidades vendidas", example = "3")
        Integer cantidad,

        @Schema(description = "Fecha y hora de la venta (si se omite, usa la hora actual del servidor)", example = "2026-10-18T10:30:00")
        LocalDateTime fechaHora
) {
    public RegistrarVentaCommand toCommand(Long tiendaId) {
        return new RegistrarVentaCommand(
                tiendaId,
                productoId,
                cantidad,
                fechaHora != null ? fechaHora : LocalDateTime.now()
        );
    }
}
