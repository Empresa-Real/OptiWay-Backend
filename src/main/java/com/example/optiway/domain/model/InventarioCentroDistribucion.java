package com.example.optiway.domain.model;

public class InventarioCentroDistribucion {

    private Long id;
    private Long centroDistribucionId;
    private Long productoId;
    private Integer cantidadActual;

    public InventarioCentroDistribucion() {}

    public InventarioCentroDistribucion(
            Long id,
            Long centroDistribucionId,
            Long productoId,
            Integer cantidadActual) {

        this.id = id;
        this.centroDistribucionId = centroDistribucionId;
        this.productoId = productoId;
        this.cantidadActual = cantidadActual;
    }

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