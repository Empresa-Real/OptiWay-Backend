package com.example.optiway.application.port.in;

import java.util.UUID;

import com.example.optiway.application.port.in.dto.InventarioResponse;

public interface AjustarInventarioCDUseCase {
    InventarioResponse ajustarInventario(UUID authUserId, Long centroDistribucionId, Long productoId, Integer nuevaCantidad);
}
