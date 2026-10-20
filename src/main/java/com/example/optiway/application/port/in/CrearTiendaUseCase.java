package com.example.optiway.application.port.in;

import com.example.optiway.application.port.in.dto.TiendaResponse;

import java.util.UUID;

public interface CrearTiendaUseCase {
    TiendaResponse crearTienda(UUID authUserId, CrearTiendaCommand command);
}
