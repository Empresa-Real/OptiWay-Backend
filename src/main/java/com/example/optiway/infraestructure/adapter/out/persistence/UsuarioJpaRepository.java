package com.example.optiway.infraestructure.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioJpaRepository
        extends JpaRepository<UsuarioJpaEntity, Long> {

        boolean existsByEmailIgnoreCase(String email);

        Optional<UsuarioJpaEntity> findByEmailIgnoreCase(String email);

        Optional<UsuarioJpaEntity> findByAuthUserId(UUID authUserId);
}
