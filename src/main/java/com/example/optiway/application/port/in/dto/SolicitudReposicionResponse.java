package com.example.optiway.application.port.in.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class SolicitudReposicionResponse {

    private Long id;
    private Long tiendaId;
    private Long productoId;
    private String nombreProducto;
    private Integer cantidad;
    private String estado; // "PENDIENTE"
    private UUID usuarioId; // O Long, dependiendo de cómo manejes los IDs de usuario en tu dominio
    private LocalDateTime fechaSolicitud;

    // Constructor, getters y setters
    public SolicitudReposicionResponse(Long id, Long tiendaId, Long productoId, String nombreProducto, 
                                       Integer cantidad, String estado, UUID usuarioId, LocalDateTime fechaSolicitud) {
        this.id = id;
        this.tiendaId = tiendaId;
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.estado = estado;
        this.usuarioId = usuarioId;
        this.fechaSolicitud = fechaSolicitud;
    }

    public Long getId() { return id; }
    public Long getTiendaId() { return tiendaId; }
    public Long getProductoId() { return productoId; }
    public String getNombreProducto() { return nombreProducto; }
    public Integer getCantidad() { return cantidad; }
    public String getEstado() { return estado; }
    public UUID getUsuarioId() { return usuarioId; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
}