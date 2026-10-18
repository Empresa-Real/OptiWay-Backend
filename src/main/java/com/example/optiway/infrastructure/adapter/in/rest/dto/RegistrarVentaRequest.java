package com.example.optiway.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record RegistrarVentaRequest(
        @NotNull(message = "El ID de la tienda es obligatorio")
        @Positive(message = "El ID de la tienda debe ser positivo")
        Long tiendaId,

        @NotNull(message = "El ID del producto es obligatorio")
        @Positive(message = "El ID del producto debe ser positivo")
        Long productoId,

        @NotNull(message = "La cantidad vendida es obligatoria")
        @Positive(message = "La cantidad vendida debe ser mayor a cero")
        Integer cantidad,

        LocalDateTime fechaHora
) {
    public RegistrarVentaRequest(Long tiendaId, Long productoId, Integer cantidad) {
        this(tiendaId, productoId, cantidad, LocalDateTime.now());
    }

    public Long getTiendaId() { return tiendaId; }
    public Long getProductoId() { return productoId; }
    public Integer getCantidad() { return cantidad; }
    public LocalDateTime getFechaHora() { return fechaHora != null ? fechaHora : LocalDateTime.now(); }
}
