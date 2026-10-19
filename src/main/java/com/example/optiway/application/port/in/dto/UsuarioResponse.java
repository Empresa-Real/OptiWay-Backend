package com.example.optiway.application.port.in.dto;

import java.util.UUID;

import com.example.optiway.domain.model.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta con información detallada de un usuario del sistema")
public record UsuarioResponse(
        @Schema(description = "Identificador único interno del usuario", example = "1")
        Long id,

        @Schema(description = "UUID del usuario autenticado en el proveedor de identidad (Supabase)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID authUserId,

        @Schema(description = "Nombre completo del usuario", example = "Ana Gonzalez")
        String nombre,

        @Schema(description = "Correo electrónico institucional del usuario", example = "ana@empresa.com")
        String email,

        @Schema(description = "Rol operativo asignado en el sistema", example = "ADMINISTRADOR")
        String rol,

        @Schema(description = "UUID del administrador que registró al usuario", example = "a1b2c3d4-e5f6-7890-abcd-1234567890ab")
        UUID creadoPor
) {
    public static UsuarioResponse from(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getAuthUserId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol() != null ? usuario.getRol().name() : null,
                usuario.getCreadoPor()
        );
    }
}
