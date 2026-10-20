package com.example.optiway.application.port.in;

import com.example.optiway.application.port.in.dto.UsuarioResponse;

import java.util.UUID;

public interface CrearUsuarioUseCase {
    UsuarioResponse crearUsuario(UUID authUserId, CrearUsuarioCommand command);
}
