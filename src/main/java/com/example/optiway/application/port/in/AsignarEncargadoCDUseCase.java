package com.example.optiway.application.port.in;

import com.example.optiway.domain.model.CentroDistribucion;

public interface AsignarEncargadoCDUseCase {
    CentroDistribucion asignarEncargado(Long centroId, Long encargadoId);
    CentroDistribucion asignarEncargado(Long centroId, Long encargadoId, java.util.UUID authUserId);
}
