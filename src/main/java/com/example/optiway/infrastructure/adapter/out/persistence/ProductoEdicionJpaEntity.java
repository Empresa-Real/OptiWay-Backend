package com.example.optiway.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "producto_ediciones")
public class ProductoEdicionJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "fecha_edicion", nullable = false)
    private Instant fechaEdicion;

    @Column(name = "usuario_auth_id", nullable = false)
    private UUID usuarioAuthId;

    public ProductoEdicionJpaEntity() {}

    public ProductoEdicionJpaEntity(Long productoId, UUID usuarioAuthId) {
        this.productoId = productoId;
        this.usuarioAuthId = usuarioAuthId;
    }

    @PrePersist
    void asignarFechaEdicion() {
        if (fechaEdicion == null) fechaEdicion = Instant.now();
    }

    public Long getId() { return id; }
    public Long getProductoId() { return productoId; }
    public Instant getFechaEdicion() { return fechaEdicion; }
    public UUID getUsuarioAuthId() { return usuarioAuthId; }
}
