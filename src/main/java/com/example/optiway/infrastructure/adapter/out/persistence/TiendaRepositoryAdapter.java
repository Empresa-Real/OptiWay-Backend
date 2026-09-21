package com.example.optiway.infrastructure.adapter.out.persistence;

import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.domain.model.Tienda;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TiendaRepositoryAdapter implements TiendaRepositoryPort {

    private final TiendaJpaRepository tiendaJpaRepository;

    public TiendaRepositoryAdapter(TiendaJpaRepository tiendaJpaRepository) {
        this.tiendaJpaRepository = tiendaJpaRepository;
    }

    @Override
    public Tienda save(Tienda tienda) {

        TiendaJpaEntity entity = new TiendaJpaEntity();

        entity.setCodigo(tienda.getCodigo());
        entity.setNombre(tienda.getNombre());
        entity.setDireccion(tienda.getDireccion());
        entity.setCiudad(tienda.getCiudad());
        entity.setEstado(tienda.getEstado());
        entity.setEncargadoId(tienda.getEncargadoId());

        TiendaJpaEntity savedEntity = tiendaJpaRepository.save(entity);

        Tienda savedTienda = new Tienda();

        savedTienda.setId(savedEntity.getId());
        savedTienda.setCodigo(savedEntity.getCodigo());
        savedTienda.setNombre(savedEntity.getNombre());
        savedTienda.setDireccion(savedEntity.getDireccion());
        savedTienda.setCiudad(savedEntity.getCiudad());
        savedTienda.setEstado(savedEntity.getEstado());
        savedTienda.setEncargadoId(savedEntity.getEncargadoId());

        return savedTienda;
    }

    @Override
    public boolean existsByCodigo(String codigo) {
        return tiendaJpaRepository.existsByCodigo(codigo);
    }

    @Override
    public List<Tienda> obtenerPorEncargadoId(Long encargadoId) {

        return tiendaJpaRepository.findByEncargadoId(encargadoId)
                .stream()
                .map(entity -> {

                    Tienda tienda = new Tienda();

                    tienda.setId(entity.getId());
                    tienda.setCodigo(entity.getCodigo());
                    tienda.setNombre(entity.getNombre());
                    tienda.setDireccion(entity.getDireccion());
                    tienda.setCiudad(entity.getCiudad());
                    tienda.setEstado(entity.getEstado());
                    tienda.setEncargadoId(entity.getEncargadoId());

                    return tienda;
                })
                .toList();
    }

    @Override
    public Optional<Tienda> obtenerPorId(Long id) {

        return tiendaJpaRepository.findById(id)
                .map(entity -> {

                    Tienda tienda = new Tienda();

                    tienda.setId(entity.getId());
                    tienda.setCodigo(entity.getCodigo());
                    tienda.setNombre(entity.getNombre());
                    tienda.setDireccion(entity.getDireccion());
                    tienda.setCiudad(entity.getCiudad());
                    tienda.setEstado(entity.getEstado());
                    tienda.setEncargadoId(entity.getEncargadoId());

                    return tienda;
                });
    }
}