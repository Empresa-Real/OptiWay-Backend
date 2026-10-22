package com.example.optiway.application.port.in.dto;

import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.domain.model.Tienda;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Asignación de ubicación (tienda o centro de distribución) de un usuario")
public record AsignacionUbicacion(
        @Schema(description = "Tipo de ubicación", example = "TIENDA")
        String tipo,

        @Schema(description = "ID de la tienda o del centro de distribución", example = "5")
        Long id,

        @Schema(description = "Nombre de la ubicación", example = "Sucursal Centro")
        String nombre
) {
    public static AsignacionUbicacion tienda(Tienda tienda) {
        return new AsignacionUbicacion("TIENDA", tienda.getId(), tienda.getNombre());
    }

    public static AsignacionUbicacion cd(CentroDistribucion centroDistribucion) {
        return new AsignacionUbicacion("CD", centroDistribucion.getId(), centroDistribucion.getNombre());
    }
}
