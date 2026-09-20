package com.example.optiway.application.port.in;

import com.example.optiway.domain.model.CentroDistribucion;
import java.util.List;

public interface CrearCentroDistribucionUseCase {
    CentroDistribucion crearCentroDistribucion(CentroDistribucion centroDistribucion);
    List<CentroDistribucion> listarCentrosDistribucion();
}