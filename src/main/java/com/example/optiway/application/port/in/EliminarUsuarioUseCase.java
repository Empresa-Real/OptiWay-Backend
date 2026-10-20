package com.example.optiway.application.port.in;

import java.util.UUID;

public interface EliminarUsuarioUseCase {
    void eliminarUsuario(UUID authUserId, Long id);
}
