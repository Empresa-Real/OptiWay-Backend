package com.example.optiway.application.port.out;

import com.example.optiway.domain.model.Tienda;

public interface TiendaRepositoryPort {
    Tienda save(Tienda tienda);
    boolean existsByCodigo(String codigo);
    java.util.List<Tienda> findAll();
}
