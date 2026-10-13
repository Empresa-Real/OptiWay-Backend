package com.example.optiway.application.port.in;

import java.util.UUID;

public interface CambiarEstadoProductoUseCase {

    CambioEstadoProductoResultado cambiarEstadoProducto(
            UUID authUserId,
            Long productoId,
            boolean activo,
            boolean confirmar
    );
}
