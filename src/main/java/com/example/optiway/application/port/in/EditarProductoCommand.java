package com.example.optiway.application.port.in;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Comando inmutable que encapsula los datos permitidos para modificar
 * un producto existente en el catálogo.
 */
public record EditarProductoCommand(
        @NotNull(message = "El ID del producto es obligatorio")
        Long id,

        String nombre,

        String categoria,

        @DecimalMin(value = "0.01", inclusive = true, message = "El precio debe ser mayor que cero")
        BigDecimal precio,

        String unidadMedida
) {
}
