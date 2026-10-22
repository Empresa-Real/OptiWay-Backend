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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventarios")
public class InventarioJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tienda_id", foreignKey = @ForeignKey(name = "fk_inventarios_tienda"))
    private TiendaJpaEntity tienda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "centro_distribucion_id", foreignKey = @ForeignKey(name = "fk_inventarios_cd"))
    private CentroDistribucionJpaEntity centroDistribucion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false, foreignKey = @ForeignKey(name = "fk_inventarios_producto"))
    private ProductoJpaEntity producto;

    @Column(name = "cantidad_actual", nullable = false)
    private Integer cantidadActual;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo;

    public InventarioJpaEntity() {
    }

    public InventarioJpaEntity(Long id, TiendaJpaEntity tienda, CentroDistribucionJpaEntity centroDistribucion,
                               ProductoJpaEntity producto, Integer cantidadActual, Integer stockMinimo) {
        this.id = id;
        this.tienda = tienda;
        this.centroDistribucion = centroDistribucion;
        this.producto = producto;
        this.cantidadActual = cantidadActual;
        this.stockMinimo = stockMinimo;
    }

    @PrePersist
    @PreUpdate
    protected void validarExclusividadUbicacion() {
        boolean tieneTienda = (this.tienda != null);
        boolean tieneCd = (this.centroDistribucion != null);
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

    public TiendaJpaEntity getTienda() {
        return tienda;
    }

    public void setTienda(TiendaJpaEntity tienda) {
        this.tienda = tienda;
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

    public Long getTiendaId() {
        return tienda != null ? tienda.getId() : null;
    }

    public Long getCentroDistribucionId() {
        return centroDistribucion != null ? centroDistribucion.getId() : null;
    }

    public Long getProductoId() {
        return producto != null ? producto.getId() : null;
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