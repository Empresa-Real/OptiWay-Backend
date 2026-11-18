package com.example.optiway.infrastructure.adapter.out.persistence;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.optiway.application.exception.RecursoNoEncontradoException;
import com.example.optiway.application.port.out.OrdenReposicionRepositoryPort;
import com.example.optiway.domain.model.OrdenReposicion;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
@Transactional
public class OrdenReposicionRepositoryAdapter implements OrdenReposicionRepositoryPort {

    private final OrdenReposicionJpaRepository repository;
    private final UsuarioJpaRepository usuarioRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public OrdenReposicionRepositoryAdapter(
            OrdenReposicionJpaRepository repository,
            UsuarioJpaRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public OrdenReposicion guardar(OrdenReposicion orden) {
        OrdenReposicionJpaEntity entity = new OrdenReposicionJpaEntity();

        entity.setRecomendacion(entityManager.getReference(
                RecomendacionReposicionJpaEntity.class, orden.getRecomendacionId()));
        entity.setProducto(entityManager.getReference(
                ProductoJpaEntity.class, orden.getProductoId()));
        entity.setCantidad(orden.getCantidad());
        entity.setCentroDistribucionOrigen(entityManager.getReference(
                CentroDistribucionJpaEntity.class, orden.getCentroDistribucionOrigenId()));
        entity.setTiendaDestino(entityManager.getReference(
                TiendaJpaEntity.class, orden.getTiendaDestinoId()));
        entity.setEstado(orden.getEstado());

        UUID authUserId = orden.getCreadoPor();
        if (authUserId != null) {
            UsuarioJpaEntity creador = usuarioRepository.findByAuthUserId(authUserId)
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "No se encontró el usuario que genera la orden de reposición."));
            entity.setCreador(creador);
        } else {
            entity.setCreador(null);
        }
        entity.setFechaCreacion(orden.getFechaCreacion());

        return toDomain(repository.save(entity));
    }

    private OrdenReposicion toDomain(OrdenReposicionJpaEntity entity) {
        OrdenReposicion orden = new OrdenReposicion();
        orden.setId(entity.getId());
        orden.setRecomendacionId(entity.getRecomendacion().getId());
        orden.setProductoId(entity.getProducto().getId());
        orden.setNombreProducto(entity.getProducto().getNombre());
        orden.setCantidad(entity.getCantidad());
        orden.setCentroDistribucionOrigenId(entity.getCentroDistribucionOrigen().getId());
        orden.setTiendaDestinoId(entity.getTiendaDestino().getId());
        orden.setEstado(entity.getEstado());
        orden.setCreadoPor(entity.getCreador() != null ? entity.getCreador().getAuthUserId() : null);
        orden.setFechaCreacion(entity.getFechaCreacion());
        return orden;
    }
}
