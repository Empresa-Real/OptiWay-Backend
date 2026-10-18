package com.example.optiway.infrastructure.adapter.in.rest.dto;

import java.time.LocalDateTime;

import com.example.optiway.domain.model.IngresoMercancia;

public record IngresoMercanciaResponse(
        Long id,
        Long centroDistribucionId,
        Long productoId,
        Integer cantidad,
        String origen,
        Long usuarioId,
        LocalDateTime fechaIngreso
) {
    public static IngresoMercanciaResponse from(IngresoMercancia ingreso) {
        return new IngresoMercanciaResponse(
                ingreso.getId(),
                ingreso.getCentroDistribucionId(),
                ingreso.getProductoId(),
                ingreso.getCantidad(),
                ingreso.getOrigen(),
                ingreso.getUsuarioId(),
                ingreso.getFechaIngreso()
        );
    }
}
