package com.example.optiway.application.port.in;

import com.example.optiway.domain.model.Inventario;

import java.util.List;
import java.util.UUID;

public interface ConsultarInventarioUseCase {

    List<Inventario> consultarInventario(
            UUID authUserId,
            Long tiendaId,
            String nombre,
            String categoria,
            Long productoId);
}