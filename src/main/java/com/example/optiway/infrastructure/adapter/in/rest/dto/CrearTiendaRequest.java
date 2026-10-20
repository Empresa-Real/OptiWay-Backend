package com.example.optiway.infrastructure.adapter.in.rest.dto;

import com.example.optiway.application.port.in.CrearTiendaCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Petición para registrar una nueva tienda en la red")
public record CrearTiendaRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Schema(description = "Nombre comercial de la tienda", example = "OptiWay Centro")
        String nombre,

        @NotBlank(message = "La dirección es obligatoria")
        @Schema(description = "Dirección física de la tienda", example = "Av. Principal 123")
        String direccion,

        @NotBlank(message = "La ciudad es obligatoria")
        @Schema(description = "Ciudad donde se ubica la tienda", example = "Bogotá")
        String ciudad,

        @Schema(description = "Identificador del encargado de la tienda (opcional)", example = "7")
        Long encargadoId
) {
    public CrearTiendaCommand toCommand() {
        return new CrearTiendaCommand(nombre, direccion, ciudad, encargadoId);
    }
}
