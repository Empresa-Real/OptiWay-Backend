package com.example.optiway.infraestructure.adapter.out.persistence;

import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.domain.model.Tienda;
import org.springframework.stereotype.Component;

@Component
public class TiendaRepositoryAdapter implements TiendaRepositoryPort {

    private final TiendaJpaRepository tiendaJpaRepository;

    public TiendaRepositoryAdapter(TiendaJpaRepository tiendaJpaRepository) {
        this.tiendaJpaRepository = tiendaJpaRepository;
    }

    @Override
    public Tienda save(Tienda tienda) {
        // Convertir el modelo de dominio a la entidad JPA
        TiendaJpaEntity entity = new TiendaJpaEntity();
        entity.setCodigo(tienda.getCodigo());
        entity.setNombre(tienda.getNombre());
        entity.setDireccion(tienda.getDireccion());
        entity.setCiudad(tienda.getCiudad());
        entity.setEstado(tienda.getEstado());

        // Guardar la entidad en la base de datos
        TiendaJpaEntity savedEntity = tiendaJpaRepository.save(entity);

        // Convertir la entidad guardada de nuevo al modelo de dominio
        Tienda savedTienda = new Tienda();
        savedTienda.setCodigo(savedEntity.getCodigo());
        savedTienda.setNombre(savedEntity.getNombre());
        savedTienda.setDireccion(savedEntity.getDireccion());
        savedTienda.setCiudad(savedEntity.getCiudad());
        savedTienda.setEstado(savedEntity.getEstado());

        return savedTienda;
    }

    @Override
    public boolean existsByCodigo(String codigo) {
        return tiendaJpaRepository.existsByCodigo(codigo);
    }
}
