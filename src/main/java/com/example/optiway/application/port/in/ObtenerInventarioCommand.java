package com.example.optiway.application.port.in;

/**
 * Comando inmutable que encapsula los filtros para consultar inventario
 * filtrando por tienda o por centro de distribución.
 * Uno de tiendaId o centroDistribucionId debe ser proporcionado (el otro es null);
 * nombre, categoria y productoId son filtros opcionales.
 */
public record ObtenerInventarioCommand(
        Long tiendaId,
        Long centroDistribucionId,
        String nombre,
        String categoria,
        Long productoId
) {
}
