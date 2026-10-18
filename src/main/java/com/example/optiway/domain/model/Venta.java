package com.example.optiway.domain.model;

import java.time.LocalDateTime;

public class Venta {

    private Long id;
    private Long tiendaId;
    private Long productoId;
    private Integer cantidad;
    private LocalDateTime fechaHora;

    public Venta() {}

    public Venta(Long id, Long tiendaId, Long productoId, Integer cantidad, LocalDateTime fechaHora) {
        this.id = id;
        this.tiendaId = tiendaId;
        this.productoId = productoId;
        setCantidad(cantidad);
        this.fechaHora = fechaHora != null ? fechaHora : LocalDateTime.now();
    }


    public void setCantidad(Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad de venta debe ser mayor a cero.");
        }
        this.cantidad = cantidad;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTiendaId() { return tiendaId; }
    public void setTiendaId(Long tiendaId) { this.tiendaId = tiendaId; }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public Integer getCantidad() { return cantidad; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
}
