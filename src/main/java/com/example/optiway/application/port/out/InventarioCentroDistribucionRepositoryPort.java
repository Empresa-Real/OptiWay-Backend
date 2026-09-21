package com.example.optiway.application.port.out;

import com.example.optiway.domain.model.InventarioCentroDistribucion;

import java.util.Optional;

public interface InventarioCentroDistribucionRepositoryPort {

    Optional<InventarioCentroDistribucion> buscarPorCentroYProducto(
            Long centroDistribucionId,
            Long productoId);

    InventarioCentroDistribucion guardar(
            InventarioCentroDistribucion inventario);
}