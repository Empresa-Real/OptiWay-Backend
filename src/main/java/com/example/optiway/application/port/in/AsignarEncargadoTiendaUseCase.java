package com.example.optiway.application.port.in;

import com.example.optiway.domain.model.Tienda;

public interface AsignarEncargadoTiendaUseCase {
    Tienda asignarEncargado(Long tiendaId, Long encargadoId);
    Tienda asignarEncargado(Long tiendaId, Long encargadoId, java.util.UUID authUserId);
}
