package com.example.optiway.domain.model;

import java.time.LocalDateTime;

public class SolicitudReposicion {

    private Long id;
    private Long tiendaId;
    private Long productoId;
    private Integer cantidad;
    private String estado;
    private Long usuarioId;
    private LocalDateTime fechaSolicitud;

    public SolicitudReposicion() {}

    public SolicitudReposicion(Long id, Long tiendaId, Long productoId, Integer cantidad, String estado, Long usuarioId, LocalDateTime fechaSolicitud) {
        this.id = id;
        this.tiendaId = tiendaId;
        this.productoId = productoId;
        setCantidad(cantidad);
        this.estado = estado != null ? estado : "PENDIENTE";
        this.usuarioId = usuarioId;
        this.fechaSolicitud = fechaSolicitud != null ? fechaSolicitud : LocalDateTime.now();
    }

    public void setCantidad(Integer cantidad) {
        if (cantidad == null || cantidad < 1) {
            throw new IllegalArgumentException("La cantidad de reposición debe ser mayor o igual a 1.");
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

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
}