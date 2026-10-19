package com.example.optiway.application.port.in;

import com.example.optiway.domain.model.Producto;

import java.util.UUID;

public interface EditarProductoUseCase {
    Producto editarProducto(UUID authUserId, Long id, Producto cambios);
}
