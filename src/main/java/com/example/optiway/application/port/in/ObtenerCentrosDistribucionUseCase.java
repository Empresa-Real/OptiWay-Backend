package com.example.optiway.application.port.in;

import com.example.optiway.application.port.in.dto.CentroDistribucionResponse;

import java.util.List;
import java.util.UUID;

public interface ObtenerCentrosDistribucionUseCase {

    List<CentroDistribucionResponse> obtenerCentrosDistribucion(UUID authUserId);
}
