package com.example.optiway.infrastructure.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

public record AsignarEncargadoRequest(
        @Schema(description = "ID numérico (Long) del usuario encargado (o null para desasignar)", example = "1")
        @Positive(message = "El ID del encargado debe ser un número positivo")
        Long encargadoId
) {
}
