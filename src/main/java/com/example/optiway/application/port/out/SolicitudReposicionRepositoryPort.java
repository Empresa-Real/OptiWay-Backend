package com.example.optiway.application.port.out;

import com.example.optiway.domain.model.SolicitudReposicion;
import java.util.List;

public interface SolicitudReposicionRepositoryPort {
    SolicitudReposicion save(SolicitudReposicion solicitud);
    List<SolicitudReposicion> findAll();
}