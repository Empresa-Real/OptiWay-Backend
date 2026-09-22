package com.example.optiway.application.port.out;

import com.example.optiway.domain.model.Inventario;

import java.util.List;

public interface InventarioRepositoryPort {

    Inventario save(Inventario inventario);

    List<Inventario> findByTiendaId(Long tiendaId);
}