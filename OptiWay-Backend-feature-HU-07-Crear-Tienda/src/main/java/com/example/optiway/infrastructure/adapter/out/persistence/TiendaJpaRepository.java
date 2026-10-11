package com.example.optiway.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
    
@Repository 
public interface TiendaJpaRepository extends JpaRepository<TiendaJpaEntity, Long> {
    boolean existsByCodigo(String codigo);
}
