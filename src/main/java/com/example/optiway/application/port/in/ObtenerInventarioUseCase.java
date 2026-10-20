package com.example.optiway.application.port.in;

import com.example.optiway.application.port.in.dto.InventarioResponse;

import java.util.List;
import java.util.UUID;

public interface ObtenerInventarioUseCase {

    /**
     * Obtiene inventario filtrando por tienda o por centro de distribución.
     * Uno de tiendaId o centroDistribucionId debe ser proporcionado (el otro es null).
     *
     * @param authUserId identificador de autenticación del usuario
     * @param command    filtros de consulta (tienda/CD, nombre, categoría, producto)
     * @return lista de respuestas de inventario enriquecidas con datos del producto
     */
    List<InventarioResponse> obtenerInventario(UUID authUserId, ObtenerInventarioCommand command);
}
