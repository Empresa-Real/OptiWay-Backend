package com.example.optiway.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

@Repository
public interface InventarioJpaRepository
        extends JpaRepository<InventarioJpaEntity, Long> {

    List<InventarioJpaEntity> findByTienda_Id(Long tiendaId);

    List<InventarioJpaEntity> findByCentroDistribucion_Id(Long centroDistribucionId);

    Optional<InventarioJpaEntity> findByTienda_IdAndProducto_Id(Long tiendaId, Long productoId);

    Optional<InventarioJpaEntity> findByCentroDistribucion_IdAndProducto_Id(Long centroDistribucionId, Long productoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventarioJpaEntity i WHERE i.tienda.id = :tiendaId AND i.producto.id = :productoId")
    Optional<InventarioJpaEntity> findByTiendaIdAndProductoIdForUpdate(
            @Param("tiendaId") Long tiendaId,
            @Param("productoId") Long productoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventarioJpaEntity i WHERE i.centroDistribucion.id = :cdId AND i.producto.id = :productoId")
    Optional<InventarioJpaEntity> findByCentroDistribucionIdAndProductoIdForUpdate(
            @Param("cdId") Long cdId,
            @Param("productoId") Long productoId);

    boolean existsByProducto_IdAndCantidadActualGreaterThan(Long productoId, Integer cantidad);
}
