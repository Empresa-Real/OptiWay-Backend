package com.example.optiway.infraestructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CentroDistribucionJpaRepository
        extends JpaRepository<CentroDistribucionJpaEntity, Long> {

    boolean existsByCodigo(String codigo);
}