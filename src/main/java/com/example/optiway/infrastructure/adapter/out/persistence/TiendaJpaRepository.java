package com.example.optiway.infrastructure.adapter.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TiendaJpaRepository extends JpaRepository<TiendaJpaEntity, Long> {
    boolean existsByCodigo(String codigo);
    List<TiendaJpaEntity> findByEncargadoId(Long encargadoId);
}
