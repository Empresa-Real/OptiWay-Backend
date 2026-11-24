package com.example.optiway.application.port.out;

import java.util.List;

import com.example.optiway.domain.model.OrdenReposicion;

public interface OrdenReposicionRepositoryPort {

    OrdenReposicion guardar(OrdenReposicion orden);

    default List<OrdenReposicion> listarPorCentroDistribucionOrigen(Long centroDistribucionId) {
        return List.of();
    }

    default boolean existeOrdenEnTransitoOEnviadaPorTienda(Long tiendaId) {
        return false;
    }
}
