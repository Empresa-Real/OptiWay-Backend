package com.example.optiway.application.port.in;
import java.util.Optional;
import java.util.UUID;

import com.example.optiway.application.port.in.dto.UsuarioResponse;

public interface ObtenerUsuarioUseCase {
    Optional<UsuarioResponse> obtenerUsuarioPorId(UUID authUserId, Long id);

    Optional<UsuarioResponse> obtenerUsuarioPorAuthId(UUID authUserId);
}
