package com.example.optiway.application.port.out;

import com.example.optiway.domain.model.Tienda;

import java.util.List;
import java.util.Optional;
public interface TiendaRepositoryPort {

    Tienda save(Tienda tienda);

    boolean existsByCodigo(String codigo);

    List<Tienda> obtenerPorEncargadoId(Long encargadoId);
    Optional<Tienda> obtenerPorId(Long id);
}