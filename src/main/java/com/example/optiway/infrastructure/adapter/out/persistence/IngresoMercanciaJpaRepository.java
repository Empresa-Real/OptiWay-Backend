package com.example.optiway.infrastructure.adapter.out.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IngresoMercanciaJpaRepository
        extends JpaRepository<IngresoMercanciaJpaEntity, Long> {

    List<IngresoMercanciaJpaEntity> findByCentroDistribucion_Id(Long centroDistribucionId);
}