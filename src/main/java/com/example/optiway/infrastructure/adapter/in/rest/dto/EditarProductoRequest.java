package com.example.optiway.infrastructure.adapter.in.rest.dto;

import com.example.optiway.application.port.in.EditarProductoCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

@Schema(description = "Petición para modificar los atributos comerciales de un producto existente")
public record EditarProductoRequest(
        @Schema(description = "Nuevo nombre comercial del producto", example = "Arroz Diana 1kg Premium")
        String nombre,

        @Schema(description = "Nueva categoría del producto", example = "Abarrotes")
        String categoria,

        @DecimalMin(value = "0.01", inclusive = true, message = "El precio debe ser mayor que cero")
        @Schema(description = "Nuevo precio de venta (debe ser mayor que cero)", example = "4800.00")
        BigDecimal precio,

        @Schema(description = "Nueva unidad de medida", example = "KG")
        String unidadMedida
) {
    public EditarProductoCommand toCommand(Long id) {
        return new EditarProductoCommand(id, nombre, categoria, precio, unidadMedida);
    }
}
