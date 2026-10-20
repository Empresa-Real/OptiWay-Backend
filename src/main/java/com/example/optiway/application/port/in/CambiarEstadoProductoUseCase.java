package com.example.optiway.application.port.in;

import com.example.optiway.application.port.in.dto.ProductoResponse;

import java.util.UUID;

public interface CambiarEstadoProductoUseCase {

    ProductoResponse cambiarEstadoProducto(UUID authUserId, CambiarEstadoProductoCommand command);
}
