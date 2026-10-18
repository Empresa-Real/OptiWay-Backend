package com.example.optiway.application.port.out;

import com.example.optiway.domain.model.IngresoMercancia;

import java.util.List;
import java.util.Optional;

public interface IngresoMercanciaRepositoryPort {

    IngresoMercancia guardar(IngresoMercancia ingreso);

    default IngresoMercancia save(IngresoMercancia ingreso) {
        return guardar(ingreso);
    }

    Optional<IngresoMercancia> findById(Long id);

    default Optional<IngresoMercancia> obtenerPorId(Long id) {
        return findById(id);
    }

    List<IngresoMercancia> findByCentroDistribucionId(Long centroDistribucionId);

    List<IngresoMercancia> findAll();
}