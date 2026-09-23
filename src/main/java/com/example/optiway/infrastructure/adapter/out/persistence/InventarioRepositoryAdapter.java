package com.example.optiway.infrastructure.adapter.out.persistence;

import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.domain.model.Inventario;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InventarioRepositoryAdapter implements InventarioRepositoryPort {

    private final InventarioJpaRepository inventarioJpaRepository;

    public InventarioRepositoryAdapter(
            InventarioJpaRepository inventarioJpaRepository) {
        this.inventarioJpaRepository = inventarioJpaRepository;
    }

    @Override
    public Inventario save(Inventario inventario) {
        inventario.validarUbicacionExclusiva();

        InventarioJpaEntity entity = new InventarioJpaEntity();
        if (inventario.getId() != null) {
            entity.setId(inventario.getId());
        }
        entity.setTiendaId(inventario.getTiendaId());
        entity.setCentroDistribucionId(inventario.getCentroDistribucionId());
        entity.setProductoId(inventario.getProductoId());
        entity.setCantidadActual(inventario.getCantidadActual());
        entity.setStockMinimo(inventario.getStockMinimo() != null ? inventario.getStockMinimo() : 0);

        InventarioJpaEntity savedEntity =
                inventarioJpaRepository.save(entity);

        return toDomain(savedEntity);
    }

    @Override
    public List<Inventario> findByTiendaId(Long tiendaId) {
        return inventarioJpaRepository.findByTiendaId(tiendaId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Inventario> findByCentroDistribucionId(Long centroDistribucionId) {
        return inventarioJpaRepository.findByCentroDistribucionId(centroDistribucionId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public java.util.Optional<Inventario> findByTiendaIdAndProductoId(Long tiendaId, Long productoId) {
        return inventarioJpaRepository.findByTiendaIdAndProductoId(tiendaId, productoId)
                .map(this::toDomain);
    }

    @Override
    public java.util.Optional<Inventario> findByCentroDistribucionIdAndProductoId(Long centroDistribucionId, Long productoId) {
        return inventarioJpaRepository.findByCentroDistribucionIdAndProductoId(centroDistribucionId, productoId)
                .map(this::toDomain);
    }

    private Inventario toDomain(InventarioJpaEntity entity) {
        Inventario inventario = new Inventario();
        inventario.setId(entity.getId());
        inventario.setTiendaId(entity.getTiendaId());
        inventario.setCentroDistribucionId(entity.getCentroDistribucionId());
        inventario.setProductoId(entity.getProductoId());
        inventario.setCantidadActual(entity.getCantidadActual());
        inventario.setStockMinimo(entity.getStockMinimo());
        return inventario;
    }
}