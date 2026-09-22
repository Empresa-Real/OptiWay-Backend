package com.example.optiway.application.port.in;

import java.util.UUID;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.Usuario;

public interface ActualizarRolUsuarioUseCase {
    Usuario actualizarRol(Long usuarioId, Rol nuevoRol, UUID adminAuthUserId);
}
