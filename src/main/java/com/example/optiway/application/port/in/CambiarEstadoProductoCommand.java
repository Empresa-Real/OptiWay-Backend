package com.example.optiway.application.port.in;

import jakarta.validation.constraints.NotNull;

public record CambiarEstadoProductoCommand(
        @NotNull(message = "El ID del producto es obligatorio")
        Long id,

        @NotNull(message = "Debe indicar si el producto estará activo o inactivo")
        Boolean activo,

        Boolean confirmarConStock
) {
}
