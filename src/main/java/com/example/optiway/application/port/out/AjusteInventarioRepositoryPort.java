
package com.example.optiway.application.port.out;

import com.example.optiway.domain.model.AjusteInventario;

import java.util.List;

public interface AjusteInventarioRepositoryPort {

    AjusteInventario guardar(AjusteInventario ajuste);

    List<AjusteInventario> findByTiendaId(Long tiendaId);
}
