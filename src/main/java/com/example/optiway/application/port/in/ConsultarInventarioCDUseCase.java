
package com.example.optiway.application.port.in;

import com.example.optiway.domain.model.Inventario;
import java.util.List;
import java.util.UUID;

public interface ConsultarInventarioCDUseCase {

    List<Inventario> consultarInventarioCD(
            UUID authUserId,
            Long centroDistribucionId,
            String nombre,
            String categoria,
            Long productoId
    );
}
