package com.example.optiway.application.port.in;

import java.util.UUID;

import com.example.optiway.application.port.in.dto.CentroDistribucionResponse;

public interface CambiarEstadoCentroDistribucionUseCase {
    CentroDistribucionResponse cambiarEstado(UUID authUserId, Long centroId, boolean activo, boolean confirmarConTiendasAsignadas);
}
