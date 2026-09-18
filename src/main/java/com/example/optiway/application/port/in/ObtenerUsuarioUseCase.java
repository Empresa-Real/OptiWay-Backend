package com.example.optiway.application.port.in;
import java.util.Optional;

import com.example.optiway.domain.model.Usuario;

public interface ObtenerUsuarioUseCase {
    Optional<Usuario> obtenerUsuario(Long id);
}
