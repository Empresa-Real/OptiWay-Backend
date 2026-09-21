package com.example.optiway.infraestructure.adapter.out.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "inventarios")
public class InventarioJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tienda_id", nullable = false)
    private Long tiendaId;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "cantidad_actual", nullable = false)
    private Integer cantidadActual;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo;

    public InventarioJpaEntity() {
    }

    public InventarioJpaEntity(Long id, Long tiendaId, Long productoId,
                               Integer cantidadActual, Integer stockMinimo) {
        this.id = id;
        this.tiendaId = tiendaId;
        this.productoId = productoId;
        this.cantidadActual = cantidadActual;
        this.stockMinimo = stockMinimo;
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