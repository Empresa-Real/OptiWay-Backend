package com.example.optiway.application.port.in;

import com.example.optiway.application.port.in.dto.ProductoResponse;

import java.util.UUID;

public interface CrearProductoUseCase {
    ProductoResponse crearProducto(UUID authUserId, CrearProductoCommand command);
}