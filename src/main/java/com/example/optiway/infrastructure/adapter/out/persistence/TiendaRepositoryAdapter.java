package com.example.optiway.infrastructure.adapter.out.persistence;

import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.domain.model.Tienda;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TiendaRepositoryAdapter implements TiendaRepositoryPort {

    private final TiendaJpaRepository tiendaJpaRepository;

    public TiendaRepositoryAdapter(TiendaJpaRepository tiendaJpaRepository) {
        this.tiendaJpaRepository = tiendaJpaRepository;
    }

    @Override
    public Tienda save(Tienda tienda) {
        TiendaJpaEntity entity = new TiendaJpaEntity();
        entity.setId(tienda.getId());
        entity.setCodigo(tienda.getCodigo());
        entity.setNombre(tienda.getNombre());
        entity.setDireccion(tienda.getDireccion());
        entity.setCiudad(tienda.getCiudad());
        entity.setActivo(tienda.isActivo());
        entity.setEncargadoId(tienda.getEncargadoId());

        TiendaJpaEntity savedEntity = tiendaJpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public boolean existsByCodigo(String codigo) {
        return tiendaJpaRepository.existsByCodigo(codigo);
    }

    @Override
    public List<Tienda> findAll() {
        return tiendaJpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Tienda> obtenerPorEncargadoId(Long encargadoId) {
        return tiendaJpaRepository.findByEncargadoId(encargadoId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Tienda> obtenerPorId(Long id) {
        return tiendaJpaRepository.findById(id)
                .map(this::toDomain);
    }

    private Tienda toDomain(TiendaJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        List<Long> centrosIds = entity.getCentrosDistribucion() != null
                ? entity.getCentrosDistribucion().stream().map(CentroDistribucionJpaEntity::getId).toList()
                : List.of();

        return new Tienda(
                entity.getId(),
                entity.getCodigo(),
                entity.getNombre(),
                entity.getDireccion(),
                entity.getCiudad(),
                entity.getActivo(),
                entity.getEncargadoId(),
                centrosIds
        );
    }
}
