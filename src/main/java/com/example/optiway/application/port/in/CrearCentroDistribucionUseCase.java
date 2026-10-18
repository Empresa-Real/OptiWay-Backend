package com.example.optiway.application.port.in;

import java.util.UUID;

import com.example.optiway.domain.model.CentroDistribucion;

public interface CrearCentroDistribucionUseCase {

    CentroDistribucion crearCentroDistribucion(CentroDistribucion centroDistribucion);

    CentroDistribucion crearCentroDistribucion(CentroDistribucion centroDistribucion, UUID authUserId);
}
