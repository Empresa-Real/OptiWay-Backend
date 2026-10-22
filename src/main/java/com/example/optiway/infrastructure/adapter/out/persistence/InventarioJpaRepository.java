package com.example.optiway.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventarioJpaRepository
        extends JpaRepository<InventarioJpaEntity, Long> {

    List<InventarioJpaEntity> findByTienda_Id(Long tiendaId);

    List<InventarioJpaEntity> findByCentroDistribucion_Id(Long centroDistribucionId);

    Optional<InventarioJpaEntity> findByTienda_IdAndProducto_Id(Long tiendaId, Long productoId);

    Optional<InventarioJpaEntity> findByCentroDistribucion_IdAndProducto_Id(Long centroDistribucionId, Long productoId);

    boolean existsByProducto_IdAndCantidadActualGreaterThan(Long productoId, Integer cantidad);
}