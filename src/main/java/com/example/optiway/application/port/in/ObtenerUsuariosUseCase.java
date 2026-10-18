package com.example.optiway.application.port.in;

import java.util.List;
import java.util.UUID;

import com.example.optiway.domain.model.Usuario;

public interface ObtenerUsuariosUseCase {

    List<Usuario> obtenerUsuarios();

    List<Usuario> obtenerUsuarios(UUID authUserId);
}