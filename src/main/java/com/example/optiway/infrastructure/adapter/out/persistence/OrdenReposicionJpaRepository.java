package com.example.optiway.infrastructure.adapter.out.persistence;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdenReposicionJpaRepository
        extends JpaRepository<OrdenReposicionJpaEntity, Long> {

    List<OrdenReposicionJpaEntity> findByCentroDistribucionOrigen_IdOrderByFechaCreacionDescIdDesc(Long centroDistribucionId);

    boolean existsByTiendaDestino_IdAndEstadoIn(Long tiendaId, Collection<String> estados);

    long countByRecomendacion_Id(Long recomendacionId);

    void deleteByRecomendacion_Id(Long recomendacionId);
}
