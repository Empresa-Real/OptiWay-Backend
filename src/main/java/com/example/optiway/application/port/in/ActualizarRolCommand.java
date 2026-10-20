package com.example.optiway.application.port.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Comando inmutable que encapsula los datos requeridos para actualizar
 * el rol de un usuario existente.
 */
public record ActualizarRolCommand(
        @NotNull(message = "El ID del usuario es obligatorio")
        Long usuarioId,

        @NotBlank(message = "El nuevo rol es obligatorio")
        String nuevoRol
) {
}
