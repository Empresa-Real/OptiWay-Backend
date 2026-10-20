package com.example.optiway.application.port.in.dto;

import java.time.LocalDateTime;

import com.example.optiway.domain.model.IngresoMercancia;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Constancia del registro de ingreso de mercancía a un centro de distribución")
public record IngresoMercanciaResponse(
        @Schema(description = "Identificador único de la operación de ingreso", example = "101")
        Long id,

        @Schema(description = "Identificador del centro de distribución de destino", example = "1")
        Long centroDistribucionId,

        @Schema(description = "Identificador del producto ingresado", example = "5")
        Long productoId,

        @Schema(description = "Cantidad de unidades ingresadas", example = "500")
        Integer cantidad,

        @Schema(description = "Proveedor o procedencia de la mercancía", example = "Proveedor Nacional S.A.")
        String origen,

        @Schema(description = "Identificador del usuario que registró el ingreso", example = "3")
        Long usuarioId,

        @Schema(description = "Fecha y hora exacta del ingreso")
        LocalDateTime fechaIngreso
) {
    public static IngresoMercanciaResponse from(IngresoMercancia ingreso) {
        if (ingreso == null) {
            return null;
        }
        return new IngresoMercanciaResponse(
                ingreso.getId(),
                ingreso.getCentroDistribucionId(),
                ingreso.getProductoId(),
                ingreso.getCantidad(),
                ingreso.getOrigen(),
                ingreso.getUsuarioId(),
                ingreso.getFechaIngreso()
        );
    }
}
