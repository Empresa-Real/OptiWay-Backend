package com.example.optiway.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentaJpaRepository extends JpaRepository<VentaJpaEntity, Long> {

    List<VentaJpaEntity> findByTiendaId(Long tiendaId);
}
