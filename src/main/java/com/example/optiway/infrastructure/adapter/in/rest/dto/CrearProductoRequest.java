package com.example.optiway.infrastructure.adapter.in.rest.dto;

import com.example.optiway.application.port.in.CrearProductoCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Schema(description = "Petición para registrar un nuevo producto en el catálogo")
public record CrearProductoRequest(
        @NotBlank(message = "El identificador es obligatorio")
        @Schema(description = "Código o identificador único comercial del producto", example = "PROD-001")
        String identificador,

        @NotBlank(message = "El nombre es obligatorio")
        @Schema(description = "Nombre comercial del producto", example = "Arroz Diana 1kg")
        String nombre,

        @NotBlank(message = "La categoría es obligatoria")
        @Schema(description = "Categoría o clasificación del producto", example = "Granos")
        String categoria,

        @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
        @Schema(description = "Precio unitario de venta", example = "4500.00")
        BigDecimal precio,

        @Schema(description = "Unidad de medida (ej: KG, UNIDAD, LITRO)", example = "KG")
        String unidadMedida
) {
    public CrearProductoCommand toCommand() {
        return new CrearProductoCommand(identificador, nombre, categoria, precio, unidadMedida);
    }
}
