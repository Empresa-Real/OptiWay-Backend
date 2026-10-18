package com.example.optiway.application.port.in;

import java.util.List;
import java.util.UUID;

import com.example.optiway.domain.model.Tienda;

public interface ObtenerTiendasUseCase {
    List<Tienda> obtenerTiendas();
    List<Tienda> obtenerTiendas(UUID authUserId);
}
