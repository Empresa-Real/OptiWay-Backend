package com.example.optiway.application.port.in.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RecomendacionReposicionResponse(
        Long id,
        Long productoId,
        String nombreProducto,
        Integer cantidad,
        Long centroDistribucionOrigenId,
        Long tiendaDestinoId,
        String estado,
        boolean fueraDeCobertura,
        UUID creadoPor,
        LocalDateTime fechaCreacion
) {
}