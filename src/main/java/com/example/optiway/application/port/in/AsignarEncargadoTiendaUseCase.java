package com.example.optiway.application.port.in;

import java.util.UUID;

import com.example.optiway.application.port.in.dto.TiendaResponse;
import com.example.optiway.infrastructure.adapter.in.rest.dto.CambiarEstadoTiendaRequest;

public interface AsignarEncargadoTiendaUseCase {
    TiendaResponse asignarEncargado(UUID authUserId, Long tiendaId, Long encargadoId);

    TiendaResponse cambiarEstado(UUID authUserId, Long tiendaId, CambiarEstadoTiendaRequest request);
}
