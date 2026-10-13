package com.example.optiway.application.port.in;

import com.example.optiway.domain.model.Producto;
import java.util.UUID;

public interface CrearProductoUseCase {
    Producto crearProducto(Producto producto, UUID authUserId);
}