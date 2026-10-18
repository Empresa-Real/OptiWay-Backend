package com.example.optiway.application.port.in;

import com.example.optiway.domain.model.Inventario;

import java.util.List;
import java.util.UUID;

public interface ConsultarInventarioUseCase {

    /**
     * Consulta inventario filtrando por tienda o por centro de distribución.
     * Uno de tiendaId o centroDistribucionId debe ser proporcionado (el otro es null).
     *
     * @param authUserId           identificador de autenticación del usuario
     * @param tiendaId             ID de la tienda (null si se consulta por centro de distribución)
     * @param centroDistribucionId ID del centro de distribución (null si se consulta por tienda)
     * @param nombre               filtro opcional por nombre de producto
     * @param categoria            filtro opcional por categoría de producto
     * @param productoId           filtro opcional por ID de producto
     * @return lista de inventarios que coinciden con los filtros
     */
    List<Inventario> consultarInventario(
            UUID authUserId,
            Long tiendaId,
            Long centroDistribucionId,
            String nombre,
            String categoria,
            Long productoId);
}
