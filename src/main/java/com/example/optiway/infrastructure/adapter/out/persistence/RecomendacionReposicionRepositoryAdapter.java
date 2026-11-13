package com.example.optiway.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.optiway.application.exception.RecursoNoEncontradoException;
import com.example.optiway.application.port.out.RecomendacionReposicionRepositoryPort;
import com.example.optiway.domain.model.RecomendacionReposicion;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
@Transactional
public class RecomendacionReposicionRepositoryAdapter
        implements RecomendacionReposicionRepositoryPort {

    private final RecomendacionReposicionJpaRepository repository;
    private final UsuarioJpaRepository usuarioRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public RecomendacionReposicionRepositoryAdapter(
            RecomendacionReposicionJpaRepository repository,
            UsuarioJpaRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public RecomendacionReposicion guardar(
            RecomendacionReposicion recomendacion) {

        RecomendacionReposicionJpaEntity entity =
                new RecomendacionReposicionJpaEntity();

        entity.setProducto(entityManager.getReference(
                ProductoJpaEntity.class, recomendacion.getProductoId()));

        entity.setCantidad(recomendacion.getCantidad());

        entity.setCentroDistribucionOrigen(entityManager.getReference(
                CentroDistribucionJpaEntity.class,
                recomendacion.getCentroDistribucionOrigenId()));

        entity.setTiendaDestino(entityManager.getReference(
                TiendaJpaEntity.class, recomendacion.getTiendaDestinoId()));

        UUID authUserId = recomendacion.getCreadoPor();

        UsuarioJpaEntity creador = usuarioRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el usuario que crea la recomendación."));

        entity.setCreador(creador);
        entity.setEstado(recomendacion.getEstado());
        entity.setFueraDeCobertura(recomendacion.isFueraDeCobertura());
        entity.setFechaCreacion(recomendacion.getFechaCreacion());

        return toDomain(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecomendacionReposicion> obtenerTodas() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private RecomendacionReposicion toDomain(
            RecomendacionReposicionJpaEntity entity) {

        RecomendacionReposicion domain = new RecomendacionReposicion();

        domain.setId(entity.getId());
        domain.setProductoId(entity.getProducto().getId());
        domain.setNombreProducto(entity.getProducto().getNombre());
        domain.setCantidad(entity.getCantidad());
        domain.setCentroDistribucionOrigenId(
                entity.getCentroDistribucionOrigen().getId());
        domain.setTiendaDestinoId(entity.getTiendaDestino().getId());
        domain.setEstado(entity.getEstado());
        domain.setFueraDeCobertura(Boolean.TRUE.equals(
                entity.getFueraDeCobertura()));
        domain.setCreadoPor(entity.getCreador().getAuthUserId());
        domain.setFechaCreacion(entity.getFechaCreacion());

        return domain;
    }
}