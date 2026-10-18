package com.example.optiway.application.port.out;

import com.example.optiway.domain.model.Venta;

import java.util.List;
import java.util.Optional;

public interface VentaRepositoryPort {

    Venta save(Venta venta);

    default Venta guardar(Venta venta) {
        return save(venta);
    }

    Optional<Venta> findById(Long id);

    default Optional<Venta> obtenerPorId(Long id) {
        return findById(id);
    }

    List<Venta> findAll();

    List<Venta> findByTiendaId(Long tiendaId);
}
