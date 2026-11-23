package com.example.optiway.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;
import com.example.optiway.application.port.out.VentaProductoResumen;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface VentaJpaRepository extends JpaRepository<VentaJpaEntity, Long> {

    List<VentaJpaEntity> findByTienda_Id(Long tiendaId);

    @Query("select new com.example.optiway.application.port.out.VentaProductoResumen(v.producto.id, sum(v.cantidad)) "
            + "from VentaJpaEntity v where v.tienda.id = :tiendaId "
            + "and v.fechaHora >= :desde and v.fechaHora < :hasta group by v.producto.id")
    List<VentaProductoResumen> resumirUnidadesVendidasPorProducto(
            @Param("tiendaId") Long tiendaId,
            @Param("desde") LocalDateTime desdeInclusive,
            @Param("hasta") LocalDateTime hastaExclusive);
}
