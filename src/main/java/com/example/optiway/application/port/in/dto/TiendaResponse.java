package com.example.optiway.application.port.in.dto;

import com.example.optiway.domain.model.Tienda;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Respuesta detallada con información de una tienda")
public record TiendaResponse(
        @Schema(description = "ID único de la tienda", example = "1")
        Long id,

        @Schema(description = "Código único de identificación de la tienda", example = "TND-A1B2C3D4")
        String codigo,

        @Schema(description = "Nombre comercial de la tienda", example = "Tienda Central Medellín")
        String nombre,

        @Schema(description = "Dirección física de la tienda", example = "Calle 50 # 45-10")
        String direccion,

        @Schema(description = "Ciudad donde se ubica la tienda", example = "Medellín")
        String ciudad,

        @Schema(description = "Estado de actividad de la tienda", example = "true")
        Boolean activo,

        @Schema(description = "Estado textual de la tienda ('Activa' o 'Inactiva')", example = "Activa")
        String estado,

        @Schema(description = "ID del usuario encargado de la tienda", example = "3")
        Long encargadoId,

        @Schema(description = "IDs de los centros de distribución que abastecen esta tienda")
        List<Long> centrosAbastecedoresIds
) {
    public static TiendaResponse from(Tienda tienda) {
        if (tienda == null) {
            return null;
        }
        boolean isActivo = tienda.isActivo() != null && tienda.isActivo();
        return new TiendaResponse(
                tienda.getId(),
                tienda.getCodigo(),
                tienda.getNombre(),
                tienda.getDireccion(),
                tienda.getCiudad(),
                isActivo,
                isActivo ? "Activa" : "Inactiva",
                tienda.getEncargadoId(),
                tienda.getCentrosAbastecedoresIds()
        );
    }
}
