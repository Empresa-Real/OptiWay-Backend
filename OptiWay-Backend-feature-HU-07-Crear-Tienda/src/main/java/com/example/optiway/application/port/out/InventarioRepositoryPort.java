package com.example.optiway.application.port.out;

import java.util.Optional;


public interface InventarioRepositoryPort {
    Optional<Integer> obtenerStock(Long tiendaId,Long productoId);
    void actualizarStock(Long tiendaId, Long productoId, Integer nuevoStock);
    boolean existeProductoEnTienda(Long tiendaId, Long productoId);
}
