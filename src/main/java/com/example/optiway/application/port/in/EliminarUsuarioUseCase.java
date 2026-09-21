package com.example.optiway.application.port.in;

import java.util.UUID;

public interface EliminarUsuarioUseCase {
    void eliminarUsuario(Long id, UUID adminAuthUserId);
}
