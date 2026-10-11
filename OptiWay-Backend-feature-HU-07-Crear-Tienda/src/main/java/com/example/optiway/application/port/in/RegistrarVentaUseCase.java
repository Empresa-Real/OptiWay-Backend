package com.example.optiway.application.port.in;

import com.example.optiway.domain.model.Venta;

public interface RegistrarVentaUseCase {
    Venta registrarVenta(RegistrarVentaCommand command);

    
}
