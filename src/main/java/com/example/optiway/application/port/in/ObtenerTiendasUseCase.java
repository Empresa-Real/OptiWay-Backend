package com.example.optiway.application.port.in;

import java.util.List;
import com.example.optiway.domain.model.Tienda;

public interface ObtenerTiendasUseCase {
    List<Tienda> obtenerTiendas();
}
