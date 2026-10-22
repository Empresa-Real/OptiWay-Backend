package com.example.optiway.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(name = "producto_ediciones")
public class ProductoEdicionJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false, foreignKey = @ForeignKey(name = "fk_producto_ediciones_producto"))
    private ProductoJpaEntity producto;

    @Column(name = "fecha_edicion", nullable = false)
    private Instant fechaEdicion;

    @OnDelete(action = OnDeleteAction.SET_NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", foreignKey = @ForeignKey(name = "fk_producto_ediciones_usuario"))
    private UsuarioJpaEntity usuario;

    public ProductoEdicionJpaEntity() {}

    public ProductoEdicionJpaEntity(ProductoJpaEntity producto, UsuarioJpaEntity usuario) {
        this.producto = producto;
        this.usuario = usuario;
    }

    @PrePersist
    void asignarFechaEdicion() {
        if (fechaEdicion == null) fechaEdicion = Instant.now();
    }

    public Long getId() { return id; }
    public ProductoJpaEntity getProducto() { return producto; }
    public void setProducto(ProductoJpaEntity producto) { this.producto = producto; }
    public Long getProductoId() { return producto != null ? producto.getId() : null; }
    public Instant getFechaEdicion() { return fechaEdicion; }
    public UsuarioJpaEntity getUsuario() { return usuario; }
    public void setUsuario(UsuarioJpaEntity usuario) { this.usuario = usuario; }
    public Long getUsuarioId() { return usuario != null ? usuario.getId() : null; }
}
