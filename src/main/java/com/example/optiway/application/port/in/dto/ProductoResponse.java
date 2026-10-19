package com.example.optiway.application.port.in.dto;

import com.example.optiway.domain.model.Producto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Respuesta detallada con información de producto del catálogo")
public record ProductoResponse(
        @Schema(description = "ID único del producto", example = "1")
        Long id,

        @Schema(description = "Código identificador de negocio", example = "PROD-001")
        String identificador,

        @Schema(description = "Nombre comercial del producto", example = "Aceite Vegetal 1L")
        String nombre,

        @Schema(description = "Categoría o línea de producto", example = "Abarrotes")
        String categoria,

        @Schema(description = "Precio unitario de venta", example = "12.50")
        BigDecimal precio,

        @Schema(description = "Unidad de medida", example = "Litro")
        String unidadMedida,

        @Schema(description = "Estado de actividad del producto en el catálogo", example = "true")
        Boolean activo,

        @Schema(description = "Estado textual para compatibilidad de interfaz ('activo' o 'inactivo')", example = "activo")
        String estado
) {
    public static ProductoResponse from(Producto producto) {
        if (producto == null) {
            return null;
        }
        boolean isActivo = producto.isActivo() != null && producto.isActivo();
        return new ProductoResponse(
                producto.getId(),
                producto.getIdentificador(),
                producto.getNombre(),
                producto.getCategoria(),
                producto.getPrecio(),
                producto.getUnidadMedida(),
                isActivo,
                isActivo ? "activo" : "inactivo"
        );
    }
}
