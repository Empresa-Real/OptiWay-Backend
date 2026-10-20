package com.example.optiway.infrastructure.adapter.in.rest.dto;

import com.example.optiway.application.port.in.RegistrarIngresoCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Petición para registrar ingreso de mercancía (el id del centro de distribución viaja en la URL)")
public record RegistrarIngresoMercanciaRequest(
        @NotNull(message = "El producto es obligatorio")
        @Schema(description = "Identificador del producto a ingresar", example = "5")
        Long productoId,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor que cero")
        @Schema(description = "Cantidad de unidades que ingresan", example = "100")
        Integer cantidad,

        @NotBlank(message = "El origen es obligatorio")
        @Schema(description = "Proveedor o procedencia de la mercancía", example = "Proveedor Central")
        String origen
) {
    public RegistrarIngresoCommand toCommand(Long centroDistribucionId) {
        return new RegistrarIngresoCommand(centroDistribucionId, productoId, cantidad, origen);
    }
}
