package com.example.optiway.application.port.in;

public record RechazarRecomendacionCommand(
        Long recomendacionId,
        String motivo
) {
}
