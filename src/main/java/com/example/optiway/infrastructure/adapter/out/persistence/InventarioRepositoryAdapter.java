package com.example.optiway.infrastructure.adapter.out.persistence;

import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.domain.model.Inventario;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class InventarioRepositoryAdapter implements InventarioRepositoryPort {

    private final InventarioJpaRepository inventarioJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

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
        if (inventario.getTiendaId() != null) {
            entity.setTienda(entityManager.getReference(TiendaJpaEntity.class, inventario.getTiendaId()));
        } else {
            entity.setTienda(null);
        }
        if (inventario.getCentroDistribucionId() != null) {
            entity.setCentroDistribucion(entityManager.getReference(CentroDistribucionJpaEntity.class, inventario.getCentroDistribucionId()));
        } else {
            entity.setCentroDistribucion(null);
        }
        if (inventario.getProductoId() != null) {
            entity.setProducto(entityManager.getReference(ProductoJpaEntity.class, inventario.getProductoId()));
        }
        entity.setCantidadActual(inventario.getCantidadActual());
        entity.setStockMinimo(inventario.getStockMinimo() != null ? inventario.getStockMinimo() : 0);

        InventarioJpaEntity savedEntity =
                inventarioJpaRepository.save(entity);

        return toDomain(savedEntity);
    }

    @Override
    public List<Inventario> findByTiendaId(Long tiendaId) {
        return inventarioJpaRepository.findByTienda_Id(tiendaId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Inventario> findByCentroDistribucionId(Long centroDistribucionId) {
        return inventarioJpaRepository.findByCentroDistribucion_Id(centroDistribucionId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Inventario> findByTiendaIdAndProductoId(Long tiendaId, Long productoId) {
        return inventarioJpaRepository.findByTienda_IdAndProducto_Id(tiendaId, productoId)
                .map(this::toDomain);
    }

    @Override
    public Optional<Inventario> findByCentroDistribucionIdAndProductoId(Long centroDistribucionId, Long productoId) {
        return inventarioJpaRepository.findByCentroDistribucion_IdAndProducto_Id(centroDistribucionId, productoId)
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

    @Override
    public boolean existeStockActivoDeProducto(Long productoId) {
        return inventarioJpaRepository.existsByProducto_IdAndCantidadActualGreaterThan(productoId, 0);
    }
}