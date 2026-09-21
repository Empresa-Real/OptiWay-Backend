package com.example.optiway.infraestructure.adapter.out.persistence;

import com.example.optiway.application.port.out.CentroDistribucionRepositoryPort;
import com.example.optiway.domain.model.CentroDistribucion;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CentroDistribucionRepositoryAdapter
        implements CentroDistribucionRepositoryPort {

    private final CentroDistribucionJpaRepository repository;

    public CentroDistribucionRepositoryAdapter(
            CentroDistribucionJpaRepository repository) {

        this.repository = repository;
    }

    @Override
    public CentroDistribucion guardar(
            CentroDistribucion centroDistribucion) {

        CentroDistribucionJpaEntity entity =
                new CentroDistribucionJpaEntity();

        entity.setId(centroDistribucion.getId());
        entity.setCodigo(centroDistribucion.getCodigo());
        entity.setNombre(centroDistribucion.getNombre());
        entity.setDireccion(centroDistribucion.getDireccion());
        entity.setCapacidad(centroDistribucion.getCapacidad());
        entity.setTiendasAbastecidasIds(
                centroDistribucion.getTiendasAbastecidasIds());

        CentroDistribucionJpaEntity saved =
                repository.save(entity);

        return toDomain(saved);
    }

    @Override
    public List<CentroDistribucion> obtenerTodos() {

        return repository.findAll()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<CentroDistribucion> obtenerPorId(Long id) {

        return repository.findById(id)
                .map(this::toDomain);
    }

    private CentroDistribucion toDomain(
            CentroDistribucionJpaEntity entity) {

        CentroDistribucion centro =
                new CentroDistribucion();

        centro.setId(entity.getId());
        centro.setCodigo(entity.getCodigo());
        centro.setNombre(entity.getNombre());
        centro.setDireccion(entity.getDireccion());
        centro.setCapacidad(entity.getCapacidad());
        centro.setTiendasAbastecidasIds(
                entity.getTiendasAbastecidasIds());

        return centro;
    }
}