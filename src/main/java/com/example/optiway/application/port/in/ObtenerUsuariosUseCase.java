package com.example.optiway.application.port.in;

import java.util.List;
import java.util.UUID;

import com.example.optiway.application.port.in.dto.UsuarioResponse;

public interface ObtenerUsuariosUseCase {

    List<UsuarioResponse> obtenerUsuarios(UUID authUserId);
}