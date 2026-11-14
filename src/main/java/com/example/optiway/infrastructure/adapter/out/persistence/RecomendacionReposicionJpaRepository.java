package com.example.optiway.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecomendacionReposicionJpaRepository
        extends JpaRepository<RecomendacionReposicionJpaEntity, Long> {
}