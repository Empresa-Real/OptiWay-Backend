package com.example.optiway.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.optiway.application.port.out.CentroDistribucionRepositoryPort;
import com.example.optiway.domain.model.CentroDistribucion;

@Component
public class CentroDistribucionRepositoryAdapter implements CentroDistribucionRepositoryPort {

    private final CentroDistribucionJpaRepository repository;

    public CentroDistribucionRepositoryAdapter(CentroDistribucionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public CentroDistribucion guardar(CentroDistribucion centroDistribucion) {
        CentroDistribucionJpaEntity entity = toEntity(centroDistribucion);
        CentroDistribucionJpaEntity savedEntity = repository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public List<CentroDistribucion> obtenerTodos() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private CentroDistribucionJpaEntity toEntity(CentroDistribucion domain) {
        CentroDistribucionJpaEntity entity = new CentroDistribucionJpaEntity();
        entity.setId(domain.getId());
        entity.setCodigo(domain.getCodigo());
        entity.setNombre(domain.getNombre());
        entity.setDireccion(domain.getDireccion());
        entity.setCapacidad(domain.getCapacidad());
        entity.setTiendasAbastecidasIds(domain.getTiendasAbastecidasIds());
        return entity;
    }

    private CentroDistribucion toDomain(CentroDistribucionJpaEntity entity) {
        return new CentroDistribucion(
                entity.getId(),
                entity.getCodigo(),
                entity.getNombre(),
                entity.getDireccion(),
                entity.getCapacidad(),
                entity.getTiendasAbastecidasIds()
        );
    }
}
