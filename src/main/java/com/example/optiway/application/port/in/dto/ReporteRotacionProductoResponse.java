package com.example.optiway.application.port.in.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Rotación de ventas y existencias actuales de un producto")
public record ReporteRotacionProductoResponse(
        Long productoId,
        String nombreProducto,
        String categoriaProducto,
        Long unidadesVendidas,
        Integer stockActual,
        @Schema(description = "Estimación de días promedio en inventario; null si no hubo ventas en el periodo")
        BigDecimal tiempoPromedioEstimadoEnInventarioDias,
        @Schema(description = "RAPIDA o BAJA según la velocidad de venta relativa en la tienda; SIN_VENTAS si no hubo ventas")
        String clasificacionRotacion) {
}
