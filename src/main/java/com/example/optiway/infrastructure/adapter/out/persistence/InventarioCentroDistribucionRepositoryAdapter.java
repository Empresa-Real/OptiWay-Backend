package com.example.optiway.infrastructure.adapter.out.persistence;

import com.example.optiway.application.port.out.InventarioCentroDistribucionRepositoryPort;
import com.example.optiway.domain.model.InventarioCentroDistribucion;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class InventarioCentroDistribucionRepositoryAdapter
        implements InventarioCentroDistribucionRepositoryPort {

    private final InventarioCentroDistribucionJpaRepository repository;

    public InventarioCentroDistribucionRepositoryAdapter(
            InventarioCentroDistribucionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<InventarioCentroDistribucion> buscarPorCentroYProducto(
            Long centroDistribucionId,
            Long productoId) {

        return repository
                .findByCentroDistribucionIdAndProductoId(
                        centroDistribucionId,
                        productoId)
                .map(this::toDomain);
    }

    @Override
    public InventarioCentroDistribucion guardar(
            InventarioCentroDistribucion inventario) {

        InventarioCentroDistribucionJpaEntity entity =
                new InventarioCentroDistribucionJpaEntity();

        entity.setId(inventario.getId());
        entity.setCentroDistribucionId(
                inventario.getCentroDistribucionId());
        entity.setProductoId(
                inventario.getProductoId());
        entity.setCantidadActual(
                inventario.getCantidadActual());

        InventarioCentroDistribucionJpaEntity saved =
                repository.save(entity);

        return toDomain(saved);
    }

    private InventarioCentroDistribucion toDomain(
            InventarioCentroDistribucionJpaEntity entity) {

        InventarioCentroDistribucion inventario =
                new InventarioCentroDistribucion();

        inventario.setId(entity.getId());
        inventario.setCentroDistribucionId(
                entity.getCentroDistribucionId());
        inventario.setProductoId(
                entity.getProductoId());
        inventario.setCantidadActual(
                entity.getCantidadActual());

        return inventario;
    }
}