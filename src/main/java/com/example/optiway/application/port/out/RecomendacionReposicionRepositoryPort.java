package com.example.optiway.application.port.out;

import java.util.List;
import com.example.optiway.domain.model.RecomendacionReposicion;

public interface RecomendacionReposicionRepositoryPort {

    RecomendacionReposicion guardar(RecomendacionReposicion recomendacion);

    List<RecomendacionReposicion> obtenerTodas();
}