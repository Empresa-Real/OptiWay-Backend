package com.example.optiway.application.port.in;

public record AjustarInventarioCommand(
        Long tiendaId,
        Long centroDistribucionId,
        Long productoId,
        Integer nuevaCantidad
) {}
