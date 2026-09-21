package com.example.optiway.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventarioCentroDistribucionJpaRepository
        extends JpaRepository<com.example.optiway.infrastructure.adapter.out.persistence.InventarioCentroDistribucionJpaEntity, Long> {

    Optional<InventarioCentroDistribucionJpaEntity>
    findByCentroDistribucionIdAndProductoId(
            Long centroDistribucionId,
            Long productoId);
}