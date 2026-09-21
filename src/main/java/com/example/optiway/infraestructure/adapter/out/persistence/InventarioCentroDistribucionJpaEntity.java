package com.example.optiway.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;

@Entity
@Table(
        name = "inventarios_centro_distribucion",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"centro_distribucion_id", "producto_id"}
        )
)
public class InventarioCentroDistribucionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "centro_distribucion_id", nullable = false)
    private Long centroDistribucionId;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "cantidad_actual", nullable = false)
    private Integer cantidadActual;

    public InventarioCentroDistribucionJpaEntity() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}