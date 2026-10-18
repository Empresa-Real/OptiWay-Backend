package com.example.optiway.infrastructure.adapter.in.rest.dto;

import com.example.optiway.domain.model.Rol;
import jakarta.validation.constraints.NotNull;

public record ActualizarRolRequest(
        @NotNull(message = "El nuevo rol es obligatorio")
        Rol rol
) {
}
