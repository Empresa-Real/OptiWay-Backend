package com.example.optiway.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventarioJpaRepository
        extends JpaRepository<InventarioJpaEntity, Long> {

    List<InventarioJpaEntity> findByTiendaId(Long tiendaId);
}