package com.example.optiway.application.port.in;

import java.util.UUID;
import com.example.optiway.application.port.in.dto.TiendaResponse;
import com.example.optiway.infrastructure.adapter.in.rest.dto.CambiarEstadoTiendaRequest;

public interface CambiarEstadoTiendaUseCase {
    TiendaResponse cambiarEstado(UUID authUserId, Long id, CambiarEstadoTiendaRequest request);
    boolean tieneOrdenesEnTransitoOEnviadas(Long tiendaId);
}