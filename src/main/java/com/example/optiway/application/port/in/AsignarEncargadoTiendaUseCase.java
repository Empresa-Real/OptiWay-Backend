package com.example.optiway.application.port.in;

import java.util.UUID;

import com.example.optiway.application.port.in.dto.TiendaResponse;

public interface AsignarEncargadoTiendaUseCase {
    TiendaResponse asignarEncargado(UUID authUserId, Long tiendaId, Long encargadoId);
}
