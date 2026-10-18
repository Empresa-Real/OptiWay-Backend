package com.example.optiway.application.port.in;

import java.util.UUID;

import com.example.optiway.domain.model.IngresoMercancia;

public interface RegistrarIngresoMercanciaUseCase {

    IngresoMercancia registrarIngreso(RegistrarIngresoCommand command, UUID authUserId);
}