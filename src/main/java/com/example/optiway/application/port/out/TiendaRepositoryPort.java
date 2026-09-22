package com.example.optiway.application.port.out;

import java.util.List;
import java.util.Optional;

import com.example.optiway.domain.model.Tienda;

public interface TiendaRepositoryPort {
    Tienda save(Tienda tienda);
    boolean existsByCodigo(String codigo);
    List<Tienda> findAll();
    List<Tienda> obtenerPorEncargadoId(Long encargadoId);
    Optional<Tienda> obtenerPorId(Long id);
}