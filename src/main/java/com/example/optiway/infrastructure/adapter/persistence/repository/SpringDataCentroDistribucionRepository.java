package com.example.optiway.infrastructure.adapter.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.optiway.infrastructure.adapter.out.persistence.CentroDistribucionJpaEntity;

public interface SpringDataCentroDistribucionRepository extends JpaRepository<CentroDistribucionJpaEntity, Long> {
}