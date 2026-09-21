package com.example.optiway.infraestructure.adapter.in.rest;

public class RegistrarIngresoMercanciaRequest {

    private Long centroDistribucionId;
    private Long productoId;
    private Integer cantidad;
    private String origen;

    public RegistrarIngresoMercanciaRequest() {
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
}