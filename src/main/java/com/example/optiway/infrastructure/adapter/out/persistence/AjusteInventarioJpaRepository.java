
package com.example.optiway.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AjusteInventarioJpaRepository
        extends JpaRepository<AjusteInventarioJpaEntity, Long> {

    List<AjusteInventarioJpaEntity> findByTienda_Id(Long tiendaId);
}
