package com.example.optiway.infrastructure.adapter.out.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "recomendaciones_reposicion")
public class RecomendacionReposicionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private ProductoJpaEntity producto;

    @Column(nullable = false)
    private Integer cantidad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "centro_distribucion_origen_id", nullable = false)
    private CentroDistribucionJpaEntity centroDistribucionOrigen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tienda_destino_id", nullable = false)
    private TiendaJpaEntity tiendaDestino;

    @Column(nullable = false, length = 30)
    private String estado;

    @Column(nullable = false)
    private Boolean fueraDeCobertura;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creado_por", nullable = false)
    private UsuarioJpaEntity creador;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    public RecomendacionReposicionJpaEntity() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ProductoJpaEntity getProducto() { return producto; }
    public void setProducto(ProductoJpaEntity producto) { this.producto = producto; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public CentroDistribucionJpaEntity getCentroDistribucionOrigen() {
        return centroDistribucionOrigen;
    }
    public void setCentroDistribucionOrigen(CentroDistribucionJpaEntity centro) {
        this.centroDistribucionOrigen = centro;
    }

    public TiendaJpaEntity getTiendaDestino() { return tiendaDestino; }
    public void setTiendaDestino(TiendaJpaEntity tiendaDestino) {
        this.tiendaDestino = tiendaDestino;
    }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Boolean getFueraDeCobertura() { return fueraDeCobertura; }
    public void setFueraDeCobertura(Boolean fueraDeCobertura) {
        this.fueraDeCobertura = fueraDeCobertura;
    }

    public UsuarioJpaEntity getCreador() { return creador; }
    public void setCreador(UsuarioJpaEntity creador) { this.creador = creador; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}