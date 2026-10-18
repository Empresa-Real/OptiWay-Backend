package com.example.optiway.application.port.in;

import java.util.UUID;

import com.example.optiway.domain.model.Tienda;

public interface CrearTiendaUseCase {
    Tienda crearTienda(Tienda tienda);
    Tienda crearTienda(Tienda tienda, UUID authUserId);
}