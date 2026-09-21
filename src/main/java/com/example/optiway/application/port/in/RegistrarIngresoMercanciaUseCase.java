package com.example.optiway.application.port.in;

import java.util.UUID;

public interface RegistrarIngresoMercanciaUseCase {

    void registrarIngreso(
            UUID authUserId,
            Long centroDistribucionId,
            Long productoId,
            Integer cantidad,
            String origen);
}