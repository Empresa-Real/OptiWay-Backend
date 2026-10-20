package com.example.optiway.infrastructure.adapter.in.rest.dto;

import com.example.optiway.application.port.in.RegistrarIngresoCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Petición para registrar ingreso de mercancía a un centro de distribución")
public class RegistrarIngresoMercanciaRequest {

    @NotNull(message = "El Centro de Distribución es obligatorio")
    @Schema(description = "Identificador del centro de distribución de destino", example = "1")
    private Long centroDistribucionId;

    @NotNull(message = "El producto es obligatorio")
    @Schema(description = "Identificador del producto a ingresar", example = "5")
    private Long productoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que cero")
    @Schema(description = "Cantidad de unidades que ingresan", example = "100")
    private Integer cantidad;

    @NotBlank(message = "El origen es obligatorio")
    @Schema(description = "Proveedor o procedencia de la mercancía", example = "Proveedor Central")
    private String origen;

    public RegistrarIngresoMercanciaRequest() {
    }

    public RegistrarIngresoMercanciaRequest(Long centroDistribucionId, Long productoId, Integer cantidad, String origen) {
        this.centroDistribucionId = centroDistribucionId;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.origen = origen;
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

    public RegistrarIngresoCommand toCommand(Long centroDistribucionId) {
        return new RegistrarIngresoCommand(centroDistribucionId, this.productoId, this.cantidad, this.origen);
    }
}
