package com.example.optiway.application.port.in;

import com.example.optiway.application.port.in.dto.ProductoResponse;

import java.util.List;
import java.util.UUID;

public interface ObtenerProductosUseCase {
    List<ProductoResponse> obtenerProductos(UUID authUserId);
}