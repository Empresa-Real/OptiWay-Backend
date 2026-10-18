package com.example.optiway.application.port.in;

import com.example.optiway.domain.model.CentroDistribucion;

import java.util.List;
import java.util.UUID;

public interface ListarCentrosDistribucionUseCase {

    List<CentroDistribucion> listarCentrosDistribucion();

    List<CentroDistribucion> listarCentrosDistribucion(UUID authUserId);
}
