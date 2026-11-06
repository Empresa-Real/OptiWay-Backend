package com.example.optiway.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class RecomendacionReposicion {

    private Long id;
    private Long productoId;
    private String nombreProducto;
    private Integer cantidad;
    private Long centroDistribucionOrigenId;
    private Long tiendaDestinoId;
    private String estado;
    private boolean fueraDeCobertura;
    private UUID creadoPor;
    private LocalDateTime fechaCreacion;

    public RecomendacionReposicion() {
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

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Long getCentroDistribucionOrigenId() {
        return centroDistribucionOrigenId;
    }

    public void setCentroDistribucionOrigenId(Long centroDistribucionOrigenId) {
        this.centroDistribucionOrigenId = centroDistribucionOrigenId;
    }

    public Long getTiendaDestinoId() {
        return tiendaDestinoId;
    }

    public void setTiendaDestinoId(Long tiendaDestinoId) {
        this.tiendaDestinoId = tiendaDestinoId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean isFueraDeCobertura() {
        return fueraDeCobertura;
    }

    public void setFueraDeCobertura(boolean fueraDeCobertura) {
        this.fueraDeCobertura = fueraDeCobertura;
    }

    public UUID getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(UUID creadoPor) {
        this.creadoPor = creadoPor;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}