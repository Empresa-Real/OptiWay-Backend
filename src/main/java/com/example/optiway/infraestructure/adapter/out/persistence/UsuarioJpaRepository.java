package com.example.optiway.infraestructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioJpaRepository
        extends JpaRepository<UsuarioJpaEntity, Long> {

        boolean existsByEmailIgnoreCase(String email);
}
