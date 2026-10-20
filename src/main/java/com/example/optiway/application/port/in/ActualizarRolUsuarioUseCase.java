package com.example.optiway.application.port.in;

import com.example.optiway.application.port.in.dto.UsuarioResponse;

import java.util.UUID;

public interface ActualizarRolUsuarioUseCase {
    UsuarioResponse actualizarRol(UUID authUserId, ActualizarRolCommand command);
}
