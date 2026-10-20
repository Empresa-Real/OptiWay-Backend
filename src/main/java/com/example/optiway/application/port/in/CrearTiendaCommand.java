package com.example.optiway.application.port.in;

import jakarta.validation.constraints.NotBlank;

/**
 * Comando inmutable que encapsula los datos requeridos para registrar
 * una nueva tienda en el sistema.
 */
public record CrearTiendaCommand(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "La dirección es obligatoria")
        String direccion,

        @NotBlank(message = "La ciudad es obligatoria")
        String ciudad,

        Long encargadoId
) {
}
