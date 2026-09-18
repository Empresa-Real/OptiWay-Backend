package com.example.optiway.application.port.in;
import java.util.Optional;
import java.util.UUID;

import com.example.optiway.domain.model.Usuario;

public interface ObtenerUsuarioUseCase {
    Optional<Usuario> obtenerUsuario(Long id);

    Optional<Usuario> obtenerUsuarioPorAuthId(UUID authUserId);
}
