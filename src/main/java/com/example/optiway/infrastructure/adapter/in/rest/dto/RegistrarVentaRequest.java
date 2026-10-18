package com.example.optiway.infrastructure.adapter.in.rest.dto;

import java.time.LocalDateTime;

public class RegistrarVentaRequest {

    private Long tiendaId;
    private Long productoId;
    private Integer cantidad;
    private LocalDateTime fechaHora;

    public RegistrarVentaRequest() {
        this.fechaHora = LocalDateTime.now();
    }

    public RegistrarVentaRequest(Long tiendaId, Long productoId, Integer cantidad, LocalDateTime fechaHora) {
        this.tiendaId = tiendaId;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.fechaHora = fechaHora != null ? fechaHora : LocalDateTime.now();
    }

    public Long getTiendaId() { return tiendaId; }
    public void setTiendaId(Long tiendaId) { this.tiendaId = tiendaId; }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
}
