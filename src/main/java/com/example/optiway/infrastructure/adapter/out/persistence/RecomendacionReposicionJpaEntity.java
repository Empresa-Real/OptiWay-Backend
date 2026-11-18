package com.example.optiway.infrastructure.adapter.out.persistence;

import java.time.LocalDateTime;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    @OnDelete(action = OnDeleteAction.SET_NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resuelto_por", foreignKey = @ForeignKey(name = "fk_recomendaciones_resuelto_por"))
    private UsuarioJpaEntity resueltoPor;

    @Column(length = 30)
    private String accionTomada;

    private LocalDateTime fechaResolucion;

    @Column(length = 500)
    private String motivoRechazo;

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

    public UsuarioJpaEntity getResueltoPor() { return resueltoPor; }
    public void setResueltoPor(UsuarioJpaEntity resueltoPor) {
        this.resueltoPor = resueltoPor;
    }

    public String getAccionTomada() { return accionTomada; }
    public void setAccionTomada(String accionTomada) {
        this.accionTomada = accionTomada;
    }

    public LocalDateTime getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(LocalDateTime fechaResolucion) {
        this.fechaResolucion = fechaResolucion;
    }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }
}