package com.example.optiway.application.port.in.dto;

import com.example.optiway.domain.model.AjusteInventario;
import java.time.LocalDateTime;

public record AjusteInventarioResponse(
        Long id,
        Long tiendaId,
        Long productoId,
        Integer cantidadAnterior,
        Integer diferencia,
        Integer cantidadNueva,
        Long usuarioId,
        LocalDateTime fechaAjuste
) {
    public static AjusteInventarioResponse from(AjusteInventario ajuste) {
        return new AjusteInventarioResponse(
                ajuste.getId(),
                ajuste.getTiendaId(),
                ajuste.getProductoId(),
                ajuste.getCantidadAnterior(),
                ajuste.getDiferencia(),
                ajuste.getCantidadNueva(),
                ajuste.getUsuarioId(),
                ajuste.getFechaAjuste()
        );
    }
}
