package com.example.optiway.application.port.in;

import java.util.List;
import java.util.UUID;
import com.example.optiway.application.port.in.dto.SolicitudReposicionResponse;
import com.example.optiway.infrastructure.adapter.in.rest.dto.CrearSolicitudReposicionRequest;

public interface SolicitarReposicionUseCase {
    SolicitudReposicionResponse crearSolicitud(UUID authUserId, Long tiendaId, CrearSolicitudReposicionRequest request);
    List<SolicitudReposicionResponse> obtenerTodasLasSolicitudes(UUID authUserId);
}