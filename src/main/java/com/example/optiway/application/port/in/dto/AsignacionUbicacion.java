package com.example.optiway.application.port.in.dto;

import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.domain.model.Tienda;

public record AsignacionUbicacion(
        String tipo,
        Long id,
        String nombre
) {
    public static AsignacionUbicacion tienda(Tienda tienda) {
        return new AsignacionUbicacion("TIENDA", tienda.getId(), tienda.getNombre());
    }

    public static AsignacionUbicacion cd(CentroDistribucion centroDistribucion) {
        return new AsignacionUbicacion("CD", centroDistribucion.getId(), centroDistribucion.getNombre());
    }
}
