package com.example.optiway.application.port.in;

import java.util.List;
import java.util.UUID;

import com.example.optiway.application.port.in.dto.TiendaResponse;

public interface ObtenerTiendasUseCase {
    List<TiendaResponse> obtenerTiendas(UUID authUserId);
}
