package com.example.optiway.application.port.in;

import java.util.UUID;

public interface VerificarStockProductoUseCase {

    boolean tieneStockActivo(UUID authUserId, Long productoId);
}
