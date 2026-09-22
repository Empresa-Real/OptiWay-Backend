package com.example.optiway.application.port.out;

import com.example.optiway.domain.model.IngresoMercancia;

public interface IngresoMercanciaRepositoryPort {

    IngresoMercancia guardar(IngresoMercancia ingreso);
}