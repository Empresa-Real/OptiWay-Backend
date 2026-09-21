package com.example.optiway.application.port.out;

import com.example.optiway.domain.model.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoRepositoryPort {

    Producto save(Producto producto);

    Optional<Producto> findById(Long id);

    List<Producto> findByNombre(String nombre);

    List<Producto> findByCategoria(String categoria);
}