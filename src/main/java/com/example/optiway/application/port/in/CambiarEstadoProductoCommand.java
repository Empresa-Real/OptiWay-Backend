package com.example.optiway.application.port.in;

import jakarta.validation.constraints.NotNull;

/**
 * Comando inmutable que encapsula los datos requeridos para activar o
 * desactivar un producto existente.
 */
public record CambiarEstadoProductoCommand(
        @NotNull(message = "El ID del producto es obligatorio")
        Long id,

        @NotNull(message = "Debe indicar si el producto estará activo o inactivo")
        Boolean activo
) {
}
