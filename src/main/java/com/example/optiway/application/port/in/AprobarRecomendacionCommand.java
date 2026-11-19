package com.example.optiway.application.port.in;

public record AprobarRecomendacionCommand(
        Long recomendacionId,
        Integer cantidadAjustada,
        Long centroDistribucionOrigenId,
        Boolean confirmarFueraDeCobertura
) {
}
