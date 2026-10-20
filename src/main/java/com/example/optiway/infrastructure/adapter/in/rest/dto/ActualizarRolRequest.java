package com.example.optiway.infrastructure.adapter.in.rest.dto;

import com.example.optiway.application.port.in.ActualizarRolCommand;
import jakarta.validation.constraints.NotNull;

public record ActualizarRolRequest(
        @NotNull(message = "El nuevo rol es obligatorio")
        String rol
) {
    public ActualizarRolCommand toCommand(Long usuarioId) {
        return new ActualizarRolCommand(usuarioId, this.rol);
    }
}
