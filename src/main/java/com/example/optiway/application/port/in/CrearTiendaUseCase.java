package com.example.optiway.application.port.in;

import com.example.optiway.domain.model.Tienda;

public interface CrearTiendaUseCase {
    Tienda crearTienda(Tienda tienda);
    Tienda crearTienda(Tienda tienda, java.util.UUID authUserId);
}