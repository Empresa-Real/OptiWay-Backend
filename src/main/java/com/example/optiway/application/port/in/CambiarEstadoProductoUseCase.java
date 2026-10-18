package com.example.optiway.application.port.in;

import com.example.optiway.domain.model.Producto;

import java.util.UUID;

public interface CambiarEstadoProductoUseCase {

    Producto cambiarEstadoProducto(UUID authUserId, Long productoId, boolean activo);
}
