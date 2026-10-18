package com.example.optiway.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.Positive;

public record AsignarEncargadoRequest(
        @Positive(message = "El ID del encargado debe ser un número positivo")
        Long encargadoId
) {
}
