package com.example.optiway.application.port.out;

import java.util.List;
import java.util.Optional;
import com.example.optiway.domain.model.RecomendacionReposicion;

public interface RecomendacionReposicionRepositoryPort {

    RecomendacionReposicion guardar(RecomendacionReposicion recomendacion);

    Optional<RecomendacionReposicion> obtenerPorIdParaActualizar(Long id);

    List<RecomendacionReposicion> obtenerTodas();
}
