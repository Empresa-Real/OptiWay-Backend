package com.example.optiway.application.port.in;

import java.util.UUID;

public interface RegistrarIngresoMercanciaUseCase {

    void registrarIngreso(RegistrarIngresoCommand command, UUID authUserId);
}