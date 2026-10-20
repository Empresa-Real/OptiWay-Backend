package com.example.optiway.application.port.in;

import com.example.optiway.application.port.in.dto.VentaResponse;

import java.util.UUID;

public interface RegistrarVentaUseCase {

    VentaResponse registrarVenta(UUID authUserId, RegistrarVentaCommand command);
}
