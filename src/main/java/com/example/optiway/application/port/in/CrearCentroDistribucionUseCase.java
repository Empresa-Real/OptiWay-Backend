package com.example.optiway.application.port.in;

import com.example.optiway.application.port.in.dto.CentroDistribucionResponse;

import java.util.UUID;

public interface CrearCentroDistribucionUseCase {

    CentroDistribucionResponse crearCentroDistribucion(UUID authUserId, CrearCentroDistribucionCommand command);
}
