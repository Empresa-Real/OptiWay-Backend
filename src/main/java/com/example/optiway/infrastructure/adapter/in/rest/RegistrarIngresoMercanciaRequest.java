package com.example.optiway.infrastructure.adapter.in.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class RegistrarIngresoMercanciaRequest {

    @NotNull(message = "El Centro de Distribución es obligatorio")
    private Long centroDistribucionId;

    @NotNull(message = "El producto es obligatorio")
    private Long productoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que cero")
    private Integer cantidad;

    @NotBlank(message = "El origen es obligatorio")
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