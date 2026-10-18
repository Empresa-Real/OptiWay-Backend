package com.example.optiway.application.port.out;

import com.example.optiway.domain.model.Inventario;

import java.util.List;
import java.util.Optional;

public interface InventarioRepositoryPort {

    Inventario save(Inventario inventario);
    default Inventario guardar(Inventario inventario) {
        return save(inventario);
    }

    List<Inventario> findByTiendaId(Long tiendaId);

    List<Inventario> findByCentroDistribucionId(Long centroDistribucionId);

    Optional<Inventario> findByTiendaIdAndProductoId(Long tiendaId, Long productoId);

    Optional<Inventario> findByCentroDistribucionIdAndProductoId(Long centroDistribucionId, Long productoId);

    boolean existeStockActivoDeProducto(Long productoId);
}