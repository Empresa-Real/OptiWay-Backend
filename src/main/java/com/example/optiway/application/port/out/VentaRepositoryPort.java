package com.example.optiway.application.port.out;

import com.example.optiway.domain.model.Venta;

import java.util.List;

public interface VentaRepositoryPort {

    Venta save(Venta venta);

    default Venta guardar(Venta venta) {
        return save(venta);
    }

    List<Venta> findByTiendaId(Long tiendaId);
}
