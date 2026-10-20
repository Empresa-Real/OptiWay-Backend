package com.example.optiway.application.port.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * Comando inmutable que encapsula los datos requeridos para registrar
 * un nuevo centro de distribución en el sistema.
 */
public record CrearCentroDistribucionCommand(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "La dirección es obligatoria")
        String direccion,

        @NotNull(message = "La capacidad es obligatoria")
        @Positive(message = "La capacidad debe ser mayor que cero")
        Integer capacidad,

        Long encargadoId,

        List<Long> tiendasAbastecidasIds
) {
}
