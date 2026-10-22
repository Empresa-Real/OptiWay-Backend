package com.example.optiway.application.port.in;

import com.example.optiway.domain.model.Rol;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Comando inmutable que encapsula los datos requeridos para registrar
 * un nuevo usuario en el sistema.
 */
public record CrearUsuarioCommand(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe tener un formato válido")
        String email,

        @NotNull(message = "El rol es obligatorio")
        Rol rol
) {
}
