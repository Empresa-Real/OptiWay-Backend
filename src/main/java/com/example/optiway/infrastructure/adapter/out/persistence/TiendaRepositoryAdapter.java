package com.example.optiway.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.domain.model.Tienda;

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
        entity.setEstado(tienda.getEstado());

        TiendaJpaEntity savedEntity = tiendaJpaRepository.save(entity);

        return new Tienda(
                savedEntity.getId(),
                savedEntity.getCodigo(),
                savedEntity.getNombre(),
                savedEntity.getDireccion(),
                savedEntity.getCiudad(),
                savedEntity.getEstado()
        );
    }

    @Override
    public boolean existsByCodigo(String codigo) {
        return tiendaJpaRepository.existsByCodigo(codigo);
    }

    @Override
    public List<Tienda> findAll() {
        return tiendaJpaRepository.findAll().stream()
                .map(e -> new Tienda(
                        e.getId(),
                        e.getCodigo(),
                        e.getNombre(),
                        e.getDireccion(),
                        e.getCiudad(),
                        e.getEstado()
                ))
                .collect(Collectors.toList());
    }
}
