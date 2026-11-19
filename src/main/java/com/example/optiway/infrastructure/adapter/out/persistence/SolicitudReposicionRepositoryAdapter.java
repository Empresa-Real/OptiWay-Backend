package com.example.optiway.infrastructure.adapter.out.persistence;

import com.example.optiway.application.port.out.SolicitudReposicionRepositoryPort;
import com.example.optiway.domain.model.SolicitudReposicion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SolicitudReposicionRepositoryAdapter implements SolicitudReposicionRepositoryPort {

    private final SolicitudReposicionJpaRepository jpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public SolicitudReposicionRepositoryAdapter(SolicitudReposicionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SolicitudReposicion save(SolicitudReposicion solicitud) {
        SolicitudReposicionJpaEntity entity = toEntity(solicitud);
        SolicitudReposicionJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<SolicitudReposicion> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private SolicitudReposicionJpaEntity toEntity(SolicitudReposicion solicitud) {
        SolicitudReposicionJpaEntity entity = new SolicitudReposicionJpaEntity();
        entity.setId(solicitud.getId());
        if (solicitud.getTiendaId() != null) {
            entity.setTienda(entityManager.getReference(TiendaJpaEntity.class, solicitud.getTiendaId()));
        }
        if (solicitud.getProductoId() != null) {
            entity.setProducto(entityManager.getReference(ProductoJpaEntity.class, solicitud.getProductoId()));
        }
        if (solicitud.getUsuarioId() != null) {
            entity.setUsuario(entityManager.getReference(UsuarioJpaEntity.class, solicitud.getUsuarioId()));
        }
        entity.setCantidad(solicitud.getCantidad());
        entity.setEstado(solicitud.getEstado());
        entity.setFechaSolicitud(solicitud.getFechaSolicitud());
        return entity;
    }

    private SolicitudReposicion toDomain(SolicitudReposicionJpaEntity entity) {
        return new SolicitudReposicion(
                entity.getId(),
                entity.getTiendaId(),
                entity.getProductoId(),
                entity.getCantidad(),
                entity.getEstado(),
                entity.getUsuarioId(),
                entity.getFechaSolicitud()
        );
    }
}