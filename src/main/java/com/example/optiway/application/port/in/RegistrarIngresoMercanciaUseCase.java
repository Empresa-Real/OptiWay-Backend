package com.example.optiway.application.port.in;

import java.util.UUID;

import com.example.optiway.application.port.in.dto.IngresoMercanciaResponse;

public interface RegistrarIngresoMercanciaUseCase {

    IngresoMercanciaResponse registrarIngreso(UUID authUserId, RegistrarIngresoCommand command);
}