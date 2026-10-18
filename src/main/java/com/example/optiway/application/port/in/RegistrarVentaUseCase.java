package com.example.optiway.application.port.in;

import com.example.optiway.domain.model.Venta;

import java.util.UUID;

public interface RegistrarVentaUseCase {

    Venta registrarVenta(RegistrarVentaCommand command, UUID authUserId);

    default Venta registrarVenta(RegistrarVentaCommand command) {
        return registrarVenta(command, null);
    }
}
