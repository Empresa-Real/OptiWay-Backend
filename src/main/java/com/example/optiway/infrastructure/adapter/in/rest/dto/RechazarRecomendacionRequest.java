package com.example.optiway.infrastructure.adapter.in.rest.dto;

import com.example.optiway.application.port.in.RechazarRecomendacionCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RechazarRecomendacionRequest(
        @NotBlank(message = "El motivo de rechazo es obligatorio.")
        @Size(max = 500, message = "El motivo de rechazo no puede superar los 500 caracteres.")
        String motivo
) {
    public RechazarRecomendacionCommand toCommand(Long recomendacionId) {
        return new RechazarRecomendacionCommand(recomendacionId, motivo);
    }
}
