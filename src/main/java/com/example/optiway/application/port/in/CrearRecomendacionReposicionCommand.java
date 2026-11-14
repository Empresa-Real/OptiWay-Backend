package com.example.optiway.application.port.in;

public record CrearRecomendacionReposicionCommand(
        Long productoId,
        Integer cantidad,
        Long centroDistribucionOrigenId,
        Long tiendaDestinoId,
        Boolean confirmarFueraDeCobertura
) {
}