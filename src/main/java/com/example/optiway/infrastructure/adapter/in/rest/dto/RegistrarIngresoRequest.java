package com.example.optiway.infrastructure.adapter.in.rest.dto;

import com.example.optiway.application.port.in.RegistrarIngresoCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class RegistrarIngresoRequest {

    @NotNull(message = "El producto es obligatorio")
    private Long productoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que cero")
    private Integer cantidad;

    @NotBlank(message = "El origen es obligatorio")
    private String origen;

    public RegistrarIngresoRequest() {
    }

    public RegistrarIngresoRequest(Long productoId, Integer cantidad, String origen) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.origen = origen;
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

    public RegistrarIngresoCommand toCommand(Long centroDistribucionId) {
        return new RegistrarIngresoCommand(centroDistribucionId, this.productoId, this.cantidad, this.origen);
    }
}
