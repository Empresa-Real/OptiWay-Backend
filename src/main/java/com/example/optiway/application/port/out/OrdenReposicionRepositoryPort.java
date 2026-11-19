package com.example.optiway.application.port.out;

import com.example.optiway.domain.model.OrdenReposicion;

public interface OrdenReposicionRepositoryPort {

    OrdenReposicion guardar(OrdenReposicion orden);

    default boolean existeOrdenEnTransitoOEnviadaPorTienda(Long tiendaId) {
        return false;
    }
}
