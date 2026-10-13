package com.example.optiway.application.port.out;

import com.example.optiway.domain.model.Producto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductoRepositoryPort {

    Producto save(Producto producto);

    Producto update(Producto producto, UUID usuarioAuthId);

    boolean existsByIdentificador(String identificador);

    List<Producto> findAll();

    Optional<Producto> findById(Long id);

    List<Producto> findByNombre(String nombre);

    List<Producto> findByCategoria(String categoria);
}
