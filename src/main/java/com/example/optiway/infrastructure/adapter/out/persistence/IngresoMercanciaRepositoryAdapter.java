package com.example.optiway.infrastructure.adapter.out.persistence;

import com.example.optiway.application.port.out.IngresoMercanciaRepositoryPort;
import com.example.optiway.domain.model.IngresoMercancia;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class IngresoMercanciaRepositoryAdapter
        implements IngresoMercanciaRepositoryPort {

    private final IngresoMercanciaJpaRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    public IngresoMercanciaRepositoryAdapter(
            IngresoMercanciaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public IngresoMercancia guardar(IngresoMercancia ingreso) {

        IngresoMercanciaJpaEntity entity = new IngresoMercanciaJpaEntity();
        if (ingreso.getCentroDistribucionId() != null) {
            entity.setCentroDistribucion(entityManager.getReference(CentroDistribucionJpaEntity.class, ingreso.getCentroDistribucionId()));
        }
        if (ingreso.getProductoId() != null) {
            entity.setProducto(entityManager.getReference(ProductoJpaEntity.class, ingreso.getProductoId()));
        }
        if (ingreso.getUsuarioId() != null) {
            entity.setUsuario(entityManager.getReference(UsuarioJpaEntity.class, ingreso.getUsuarioId()));
        } else {
            entity.setUsuario(null);
        }
        entity.setCantidad(ingreso.getCantidad());
        entity.setOrigen(ingreso.getOrigen());
        entity.setFechaIngreso(ingreso.getFechaIngreso());

        IngresoMercanciaJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<IngresoMercancia> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<IngresoMercancia> findByCentroDistribucionId(Long centroDistribucionId) {
        return repository.findByCentroDistribucionId(centroDistribucionId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<IngresoMercancia> findAll() {
        return repository.findAll()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private IngresoMercancia toDomain(IngresoMercanciaJpaEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La entidad IngresoMercanciaJpaEntity no puede ser nula");
        }
        IngresoMercancia resultado = new IngresoMercancia();
        resultado.setId(entity.getId());
        resultado.setCentroDistribucionId(entity.getCentroDistribucionId());
        resultado.setProductoId(entity.getProductoId());
        resultado.setCantidad(entity.getCantidad());
        resultado.setOrigen(entity.getOrigen());
        resultado.setUsuarioId(entity.getUsuarioId());
        resultado.setFechaIngreso(entity.getFechaIngreso());
        return resultado;
    }
}
