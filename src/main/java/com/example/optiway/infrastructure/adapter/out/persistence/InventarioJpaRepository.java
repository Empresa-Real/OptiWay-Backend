package com.example.optiway.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventarioJpaRepository
        extends JpaRepository<InventarioJpaEntity, Long> {

    List<InventarioJpaEntity> findByTiendaId(Long tiendaId);

    List<InventarioJpaEntity> findByCentroDistribucionId(Long centroDistribucionId);

    Optional<InventarioJpaEntity> findByTiendaIdAndProductoId(Long tiendaId, Long productoId);

    Optional<InventarioJpaEntity> findByCentroDistribucionIdAndProductoId(Long centroDistribucionId, Long productoId);

    boolean existsByProductoIdAndCantidadActualGreaterThan(Long productoId, Integer cantidad);
}