package com.example.optiway.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "inventarios")
public class InventarioJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tienda_id", nullable = true)
    private Long tiendaId;

    @Column(name = "centro_distribucion_id", nullable = true)
    private Long centroDistribucionId;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "cantidad_actual", nullable = false)
    private Integer cantidadActual;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo;

    public InventarioJpaEntity() {
    }

    public InventarioJpaEntity(Long id, Long tiendaId, Long centroDistribucionId, Long productoId,
                               Integer cantidadActual, Integer stockMinimo) {
        this.id = id;
        this.tiendaId = tiendaId;
        this.centroDistribucionId = centroDistribucionId;
        this.productoId = productoId;
        this.cantidadActual = cantidadActual;
        this.stockMinimo = stockMinimo;
    }

    @PrePersist
    @PreUpdate
    private void validarExclusividadUbicacion() {
        boolean tieneTienda = (this.tiendaId != null);
        boolean tieneCd = (this.centroDistribucionId != null);
        if (tieneTienda && tieneCd) {
            throw new IllegalStateException(
                    "Un registro de inventario no puede tener asociados simultáneamente una tienda y un centro de distribución.");
        }
        if (!tieneTienda && !tieneCd) {
            throw new IllegalStateException(
                    "El registro de inventario debe estar asociado obligatoriamente a una tienda o a un centro de distribución.");
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTiendaId() {
        return tiendaId;
    }

    public void setTiendaId(Long tiendaId) {
        this.tiendaId = tiendaId;
    }

    public Long getCentroDistribucionId() {
        return centroDistribucionId;
    }

    public void setCentroDistribucionId(Long centroDistribucionId) {
        this.centroDistribucionId = centroDistribucionId;
    }

    public Long getProductoId() {
        return productoId;
    } 

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public Integer getCantidadActual() {
        return cantidadActual;
    }

    public void setCantidadActual(Integer cantidadActual) {
        this.cantidadActual = cantidadActual;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }
}