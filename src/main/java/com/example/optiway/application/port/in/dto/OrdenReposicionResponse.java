package com.example.optiway.application.port.in.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.optiway.domain.model.OrdenReposicion;

public record OrdenReposicionResponse(
        Long id,
        Long recomendacionId,
        Long productoId,
        String nombreProducto,
        Integer cantidad,
        Long centroDistribucionOrigenId,
        Long tiendaDestinoId,
        String estado,
        UUID creadoPor,
        LocalDateTime fechaCreacion
) {
    public static OrdenReposicionResponse from(OrdenReposicion orden) {
        return new OrdenReposicionResponse(
                orden.getId(),
                orden.getRecomendacionId(),
                orden.getProductoId(),
                orden.getNombreProducto(),
                orden.getCantidad(),
                orden.getCentroDistribucionOrigenId(),
                orden.getTiendaDestinoId(),
                orden.getEstado(),
                orden.getCreadoPor(),
                orden.getFechaCreacion()
        );
    }
}
