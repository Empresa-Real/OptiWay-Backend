package com.example.optiway.application.port.in.dto;

import java.time.LocalDateTime;

import com.example.optiway.domain.model.Venta;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta con el detalle de una transacción de venta registrada")
public record VentaResponse(
        @Schema(description = "Identificador único de la transacción de venta", example = "1")
        Long id,

        @Schema(description = "Identificador de la tienda donde ocurrió la venta", example = "1")
        Long tiendaId,

        @Schema(description = "Identificador del producto vendido", example = "5")
        Long productoId,

        @Schema(description = "Cantidad de unidades vendidas", example = "3")
        Integer cantidad,

        @Schema(description = "Fecha y hora en que se registró la venta")
        LocalDateTime fechaHora
) {
    public static VentaResponse from(Venta venta) {
        if (venta == null) {
            return null;
        }
        return new VentaResponse(
                venta.getId(),
                venta.getTiendaId(),
                venta.getProductoId(),
                venta.getCantidad(),
                venta.getFechaHora()
        );
    }
}
