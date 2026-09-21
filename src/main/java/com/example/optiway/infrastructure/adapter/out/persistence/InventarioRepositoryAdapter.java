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

        InventarioJpaEntity entity = new InventarioJpaEntity();

        entity.setTiendaId(inventario.getTiendaId());
        entity.setProductoId(inventario.getProductoId());
        entity.setCantidadActual(inventario.getCantidadActual());
        entity.setStockMinimo(inventario.getStockMinimo());

        InventarioJpaEntity savedEntity =
                inventarioJpaRepository.save(entity);

        Inventario savedInventario = new Inventario();

        savedInventario.setId(savedEntity.getId());
        savedInventario.setTiendaId(savedEntity.getTiendaId());
        savedInventario.setProductoId(savedEntity.getProductoId());
        savedInventario.setCantidadActual(savedEntity.getCantidadActual());
        savedInventario.setStockMinimo(savedEntity.getStockMinimo());

        return savedInventario;
    }

    @Override
    public List<Inventario> findByTiendaId(Long tiendaId) {

        return inventarioJpaRepository.findByTiendaId(tiendaId)
                .stream()
                .map(entity -> {

                    Inventario inventario = new Inventario();

                    inventario.setId(entity.getId());
                    inventario.setTiendaId(entity.getTiendaId());
                    inventario.setProductoId(entity.getProductoId());
                    inventario.setCantidadActual(
                            entity.getCantidadActual());
                    inventario.setStockMinimo(
                            entity.getStockMinimo());

                    return inventario;
                })
                .toList();
    }
}