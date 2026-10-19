package com.example.optiway.application.port.in.dto;

import com.example.optiway.domain.model.Inventario;
import com.example.optiway.domain.model.Producto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta detallada con información de inventario y datos enriquecidos del producto")
public record InventarioResponse(
        @Schema(description = "ID único del registro de inventario", example = "1")
        Long id,

        @Schema(description = "ID de la tienda (null si pertenece a centro de distribución)", example = "10")
        Long tiendaId,

        @Schema(description = "ID del centro de distribución (null si pertenece a tienda)", example = "null")
        Long centroDistribucionId,

        @Schema(description = "ID del producto", example = "5")
        Long productoId,

        @Schema(description = "Nombre comercial del producto", example = "Camisa Oxford Clásica")
        String nombreProducto,

        @Schema(description = "Categoría del producto", example = "Ropa")
        String categoriaProducto,

        @Schema(description = "Cantidad actual de unidades disponibles en stock", example = "120")
        Integer cantidadActual,

        @Schema(description = "Cantidad mínima requerida antes de alertar stock bajo", example = "20")
        Integer stockMinimo,

        @Schema(description = "Indica si el stock actual es menor al stock mínimo configurado", example = "false")
        boolean stockBajo
) {
    public static InventarioResponse from(Inventario inventario, Producto producto) {
        String nombre = producto != null ? producto.getNombre() : null;
        String categoria = producto != null ? producto.getCategoria() : null;
        return new InventarioResponse(
                inventario.getId(),
                inventario.getTiendaId(),
                inventario.getCentroDistribucionId(),
                inventario.getProductoId(),
                nombre,
                categoria,
                inventario.getCantidadActual(),
                inventario.getStockMinimo(),
                inventario.calcularStockBajo()
        );
    }
}
