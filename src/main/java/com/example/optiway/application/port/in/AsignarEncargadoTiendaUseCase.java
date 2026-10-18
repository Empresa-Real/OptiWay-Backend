package com.example.optiway.application.port.in;

import java.util.UUID;

import com.example.optiway.domain.model.Tienda;

public interface AsignarEncargadoTiendaUseCase {
    Tienda asignarEncargado(Long tiendaId, Long encargadoId);
    Tienda asignarEncargado(Long tiendaId, Long encargadoId, UUID authUserId);
}
