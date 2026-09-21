package com.example.optiway.application.port.out;

import java.util.List;
import java.util.Optional;

import com.example.optiway.domain.model.CentroDistribucion;

public interface CentroDistribucionRepositoryPort {

    CentroDistribucion guardar(
            CentroDistribucion centroDistribucion);

    List<CentroDistribucion> obtenerTodos();

    Optional<CentroDistribucion> obtenerPorId(Long id);
}