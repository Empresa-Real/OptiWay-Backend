package com.example.optiway.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CentroDistribucionJpaRepository extends JpaRepository<CentroDistribucionJpaEntity, Long> {
    boolean existsByCodigo(String codigo);
    List<CentroDistribucionJpaEntity> findByEncargado_Id(Long encargadoId);
}
