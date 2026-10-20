package com.example.optiway.application.port.in;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * Comando inmutable que encapsula los datos requeridos para registrar
 * un nuevo producto en el catálogo.
 */
public record CrearProductoCommand(
        @NotBlank(message = "El identificador es obligatorio")
        String identificador,

        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "La categoría es obligatoria")
        String categoria,

        @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
        BigDecimal precio,

        String unidadMedida
) {
}
