package com.example.optiway.infrastructure.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VentaJpaRepository extends JpaRepository<VentaJpaEntity, Long> {

    List<VentaJpaEntity> findByTienda_Id(Long tiendaId);
    @Query("""
        SELECT v
        FROM VentaJpaEntity v
        WHERE v.producto.id = :productoId
          AND v.tienda.id IN (:tiendaIds)
          AND v.fechaHora >= :desdeInclusive
          AND v.fechaHora < :hastaExclusivo
        ORDER BY v.tienda.id, v.fechaHora, v.id
        """)
List<VentaJpaEntity> buscarVentasPorProductoYTiendasEnPeriodo(
        @Param("productoId") Long productoId,
        @Param("tiendaIds") List<Long> tiendaIds,
        @Param("desdeInclusive") LocalDateTime desdeInclusive,
        @Param("hastaExclusivo") LocalDateTime hastaExclusivo
);
}
