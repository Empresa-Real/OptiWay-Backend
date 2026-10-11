package com.example.optiway.domain.model;

import java.time.LocalDateTime;

public class Venta {
    private Long id;
    private Long productoId;
    private Integer cantidad;
    private LocalDateTime fechaHora;
    private Long tiendaId;

   public Venta(Long id, Long tiendaId, Long productoId, Integer cantidad, LocalDateTime fechaHora) {
        this.id = id;
        this.tiendaId = tiendaId;
        this.productoId = productoId;
        setCantidad(cantidad);
        this.fechaHora = fechaHora != null ? fechaHora : LocalDateTime.now();
    }

    public Venta(Long tiendaId, Long productoId, Integer cantidad, LocalDateTime fechaHora) {
        this(null, tiendaId, productoId, cantidad, fechaHora);
    }

    private void setCantidad(Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad de venta debe ser mayor a cero.");
        }
        this.cantidad = cantidad;
    }

    // Getters
    public Long getId() { return id; }
    public Long getTiendaId() { return tiendaId; }
    public Long getProductoId() { return productoId; }
    public Integer getCantidad() { return cantidad; }
    public LocalDateTime getFechaHora() { return fechaHora; }
}