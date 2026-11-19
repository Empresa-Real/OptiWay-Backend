package com.example.optiway.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotNull;

public record CambiarEstadoCDRequest(
        @NotNull(message = "El campo activo es obligatorio")
        Boolean activo,

        Boolean confirmarConTiendasAsignadas
) {
}
