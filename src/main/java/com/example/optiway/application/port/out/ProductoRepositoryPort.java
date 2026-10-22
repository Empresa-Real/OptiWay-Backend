package com.example.optiway.application.port.out;

import com.example.optiway.domain.model.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoRepositoryPort {

    Producto save(Producto producto);

    default Producto guardar(Producto producto) {
        return save(producto);
    }

    Producto update(Producto producto, Long usuarioId);

    boolean existsByIdentificador(String identificador);

    List<Producto> findAll();
 
    List<Producto> findAllById(List<Long> ids);

    Optional<Producto> findById(Long id);

    List<Producto> findByNombre(String nombre);

    List<Producto> findByCategoria(String categoria);
}
