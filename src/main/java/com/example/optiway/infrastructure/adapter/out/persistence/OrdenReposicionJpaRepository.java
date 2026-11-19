package com.example.optiway.infrastructure.adapter.out.persistence;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdenReposicionJpaRepository
        extends JpaRepository<OrdenReposicionJpaEntity, Long> {

    boolean existsByTiendaDestino_IdAndEstadoIn(Long tiendaId, Collection<String> estados);

    long countByRecomendacion_Id(Long recomendacionId);

    void deleteByRecomendacion_Id(Long recomendacionId);
}
