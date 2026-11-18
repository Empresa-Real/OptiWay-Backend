package com.example.optiway.infrastructure.adapter.in.rest.dto;

import com.example.optiway.application.port.in.AprobarRecomendacionCommand;
import jakarta.validation.constraints.Positive;

public record AprobarRecomendacionRequest(
        @Positive(message = "La cantidad ajustada debe ser un número entero positivo.")
        Integer cantidadAjustada,

        @Positive(message = "El ID del Centro de Distribución de origen debe ser positivo.")
        Long centroDistribucionOrigenId,

        Boolean confirmarFueraDeCobertura
) {
    public AprobarRecomendacionCommand toCommand(Long recomendacionId) {
        return new AprobarRecomendacionCommand(
                recomendacionId,
                cantidadAjustada,
                centroDistribucionOrigenId,
                confirmarFueraDeCobertura
        );
    }
}
