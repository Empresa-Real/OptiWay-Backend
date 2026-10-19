package com.example.optiway.application.port.in;

import java.util.UUID;

import com.example.optiway.domain.model.CentroDistribucion;

public interface AsignarEncargadoCDUseCase {
    CentroDistribucion asignarEncargado(Long centroId, Long encargadoId, UUID authUserId);
}
