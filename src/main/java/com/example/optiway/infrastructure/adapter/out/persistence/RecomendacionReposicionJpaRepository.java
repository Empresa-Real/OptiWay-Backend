package com.example.optiway.infrastructure.adapter.out.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

@Repository
public interface RecomendacionReposicionJpaRepository
        extends JpaRepository<RecomendacionReposicionJpaEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RecomendacionReposicionJpaEntity r WHERE r.id = :id")
    Optional<RecomendacionReposicionJpaEntity> findByIdForUpdate(@Param("id") Long id);
}
