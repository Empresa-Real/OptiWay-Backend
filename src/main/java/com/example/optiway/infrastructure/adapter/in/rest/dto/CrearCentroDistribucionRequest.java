package com.example.optiway.infrastructure.adapter.in.rest.dto;

import com.example.optiway.application.port.in.CrearCentroDistribucionCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

@Schema(description = "Petición para registrar un nuevo centro de distribución")
public record CrearCentroDistribucionRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Schema(description = "Nombre del centro de distribución", example = "CD Norte")
        String nombre,

        @NotBlank(message = "La dirección es obligatoria")
        @Schema(description = "Dirección física del centro de distribución", example = "Zona Industrial 45")
        String direccion,

        @NotNull(message = "La capacidad es obligatoria")
        @Positive(message = "La capacidad debe ser mayor que cero")
        @Schema(description = "Capacidad máxima de unidades del centro", example = "5000")
        Integer capacidad,

        @Schema(description = "Identificador del encargado del CD (opcional)", example = "7")
        Long encargadoId,

        @Schema(description = "Identificadores de las tiendas que abastece el centro", example = "[1, 2]")
        List<Long> tiendasAbastecidasIds
) {
    public CrearCentroDistribucionCommand toCommand() {
        return new CrearCentroDistribucionCommand(nombre, direccion, capacidad,
                encargadoId, tiendasAbastecidasIds);
    }
}
