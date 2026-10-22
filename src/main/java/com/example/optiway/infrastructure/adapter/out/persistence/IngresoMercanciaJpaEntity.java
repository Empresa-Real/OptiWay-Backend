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
import jakarta.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "ingresos_mercancia")
public class IngresoMercanciaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "centro_distribucion_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ingresos_cd"))
    private CentroDistribucionJpaEntity centroDistribucion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ingresos_producto"))
    private ProductoJpaEntity producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private String origen;

    @OnDelete(action = OnDeleteAction.SET_NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", foreignKey = @ForeignKey(name = "fk_ingresos_usuario"))
    private UsuarioJpaEntity usuario;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDateTime fechaIngreso;

    public IngresoMercanciaJpaEntity() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CentroDistribucionJpaEntity getCentroDistribucion() {
        return centroDistribucion;
    }

    public void setCentroDistribucion(CentroDistribucionJpaEntity centroDistribucion) {
        this.centroDistribucion = centroDistribucion;
    }

    public ProductoJpaEntity getProducto() {
        return producto;
    }

    public void setProducto(ProductoJpaEntity producto) {
        this.producto = producto;
    }

    public UsuarioJpaEntity getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioJpaEntity usuario) {
        this.usuario = usuario;
    }

    public Long getCentroDistribucionId() {
        return centroDistribucion != null ? centroDistribucion.getId() : null;
    }

    public Long getProductoId() {
        return producto != null ? producto.getId() : null;
    }

    public Long getUsuarioId() {
        return usuario != null ? usuario.getId() : null;
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

    public LocalDateTime getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDateTime fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }
}