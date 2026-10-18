package com.example.optiway.application.port.out;

import com.example.optiway.domain.model.Producto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductoRepositoryPort {

    Producto save(Producto producto);

    default Producto guardar(Producto producto) {
        return save(producto);
    }

    Producto update(Producto producto, UUID authUserId);

    boolean existsByIdentificador(String identificador);

    List<Producto> findAll();
 
    List<Producto> findAllById(List<Long> ids);

    default List<Producto> findAllById(Iterable<Long> ids) {
        if (ids == null) return List.of();
        if (ids instanceof List<Long> list) return findAllById(list);
        List<Long> list = new java.util.ArrayList<>();
        ids.forEach(list::add);
        return findAllById(list);
    }

    Optional<Producto> findById(Long id);

    List<Producto> findByNombre(String nombre);

    List<Producto> findByCategoria(String categoria);
}
