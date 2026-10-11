package com.example.optiway.application.port.out;

import com.example.optiway.domain.model.Venta;

public interface VentaRepositoryPort {
    Venta guardar(Venta venta);
    
}
