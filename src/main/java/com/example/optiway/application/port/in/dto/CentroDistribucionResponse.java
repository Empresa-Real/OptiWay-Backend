package com.example.optiway.application.port.in.dto;

import java.util.List;

import com.example.optiway.domain.model.CentroDistribucion;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta con la información de un centro de distribución")
public record CentroDistribucionResponse(
        @Schema(description = "Identificador único interno del centro de distribución", example = "1")
        Long id,

        @Schema(description = "Código comercial único generado por el sistema", example = "CD-001")
        String codigo,

        @Schema(description = "Nombre o denominación del centro de distribución", example = "CD Principal Valle de Aburrá")
        String nombre,

        @Schema(description = "Dirección física de ubicación", example = "Autopista Sur # 65-120, Itagüí")
        String direccion,

        @Schema(description = "Capacidad máxima de almacenamiento en unidades", example = "15000")
        Integer capacidad,

        @Schema(description = "ID del usuario encargado asignado", example = "4")
        Long encargadoId,

        @Schema(description = "Lista de IDs de las tiendas abastecidas por este centro", example = "[1, 2, 3]")
        List<Long> tiendasAbastecidasIds
) {
    public static CentroDistribucionResponse from(CentroDistribucion cd) {
        if (cd == null) {
            return null;
        }
        return new CentroDistribucionResponse(
                cd.getId(),
                cd.getCodigo(),
                cd.getNombre(),
                cd.getDireccion(),
                cd.getCapacidad(),
                cd.getEncargadoId(),
                cd.getTiendasAbastecidasIds()
        );
    }
}
