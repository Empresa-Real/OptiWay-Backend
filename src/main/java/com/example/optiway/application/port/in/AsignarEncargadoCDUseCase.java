package com.example.optiway.application.port.in;

import java.util.UUID;

import com.example.optiway.application.port.in.dto.CentroDistribucionResponse;

public interface AsignarEncargadoCDUseCase {
    CentroDistribucionResponse asignarEncargado(UUID authUserId, Long centroId, Long encargadoId);
}
