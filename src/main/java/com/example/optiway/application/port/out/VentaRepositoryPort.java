package com.example.optiway.application.port.out;

import com.example.optiway.domain.model.Venta;

import java.util.List;

public interface VentaRepositoryPort {

    Venta guardar(Venta venta);

    List<Venta> findByTiendaId(Long tiendaId);
}
