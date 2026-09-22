package com.example.optiway.application.port.in;

import com.example.optiway.domain.model.CentroDistribucion;
import java.util.List;

public interface CrearCentroDistribucionUseCase {
    CentroDistribucion crearCentroDistribucion(CentroDistribucion centroDistribucion);
    CentroDistribucion crearCentroDistribucion(CentroDistribucion centroDistribucion, java.util.UUID authUserId);
    List<CentroDistribucion> listarCentrosDistribucion();
    List<CentroDistribucion> listarCentrosDistribucion(java.util.UUID authUserId);
}