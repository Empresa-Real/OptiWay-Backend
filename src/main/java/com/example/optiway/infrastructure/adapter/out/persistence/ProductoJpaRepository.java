package com.example.optiway.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoJpaRepository extends JpaRepository<ProductoJpaEntity, Long> {

    List<ProductoJpaEntity> findByNombreContainingIgnoreCase(String nombre);

    List<ProductoJpaEntity> findByCategoriaIgnoreCase(String categoria);
}