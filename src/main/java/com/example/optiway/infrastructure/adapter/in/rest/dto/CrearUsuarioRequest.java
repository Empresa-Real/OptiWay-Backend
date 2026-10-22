package com.example.optiway.infrastructure.adapter.in.rest.dto;

import com.example.optiway.application.port.in.CrearUsuarioCommand;
import com.example.optiway.domain.model.Rol;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Petición para crear un usuario en el sistema")
public record CrearUsuarioRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Schema(description = "Nombre completo del usuario", example = "Ana Torres")
        String nombre,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe tener un formato válido")
        @Schema(description = "Correo electrónico del usuario (identifier de Supabase Auth)", example = "ana@optiway.com")
        String email,

        @NotNull(message = "El rol es obligatorio")
        @Schema(description = "Rol asignado al usuario", example = "ENCARGADO_TIENDA")
        Rol rol
) {
    public CrearUsuarioCommand toCommand() {
        return new CrearUsuarioCommand(nombre, email, rol);
    }
}
