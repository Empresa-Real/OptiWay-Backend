package com.example.optiway.domain.model;

import java.time.LocalDateTime;

public class IngresoMercancia {

    private Long id;
    private Long productoId;
    private Integer cantidad;
    private String origen;
    private Long usuarioId;
    private LocalDateTime fechaIngreso;
    private Long centroDistribucionId;

    public IngresoMercancia() {
    }

    public IngresoMercancia(Long id,
                            Long centroDistribucionId,
                            Long productoId,
                            Integer cantidad,
                            String origen,
                            Long usuarioId,
                            LocalDateTime fechaIngreso) {
        this.id = id;
        this.centroDistribucionId = centroDistribucionId;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.origen = origen;
        this.usuarioId = usuarioId;
        this.fechaIngreso = fechaIngreso;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public LocalDateTime getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDateTime fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }
    public Long getCentroDistribucionId() {
        return centroDistribucionId;
    }

    public void setCentroDistribucionId(Long centroDistribucionId) {
        this.centroDistribucionId = centroDistribucionId;
    }
}
