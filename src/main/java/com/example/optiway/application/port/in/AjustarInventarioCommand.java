package com.example.optiway.application.port.in;

public record AjustarInventarioCommand(
        Long tiendaId,
        Long productoId,
        Integer diferencia
) {}
