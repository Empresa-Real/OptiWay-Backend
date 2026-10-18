package com.example.optiway.application.service;

import com.example.optiway.application.port.in.ConsultarInventarioCDUseCase;
import com.example.optiway.application.port.out.CentroDistribucionRepositoryPort;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.domain.model.Inventario;
import com.example.optiway.domain.model.Producto;
import com.example.optiway.infrastructure.security.AuthorizationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ConsultarInventarioCDService implements ConsultarInventarioCDUseCase {

    private final InventarioRepositoryPort inventarioRepositoryPort;
    private final ProductoRepositoryPort productoRepositoryPort;
    private final CentroDistribucionRepositoryPort centroDistribucionRepositoryPort;
    private final AuthorizationService authorizationService;

    public ConsultarInventarioCDService(
            InventarioRepositoryPort inventarioRepositoryPort,
            ProductoRepositoryPort productoRepositoryPort,
            CentroDistribucionRepositoryPort centroDistribucionRepositoryPort,
            AuthorizationService authorizationService) {

        this.inventarioRepositoryPort = inventarioRepositoryPort;
        this.productoRepositoryPort = productoRepositoryPort;
        this.centroDistribucionRepositoryPort = centroDistribucionRepositoryPort;
        this.authorizationService = authorizationService;
    }

    @Override
    public List<Inventario> consultarInventarioCD(
            UUID authUserId,
            Long centroDistribucionId,
            String nombre,
            String categoria,
            Long productoId) {

        CentroDistribucion centro = centroDistribucionRepositoryPort
                .obtenerPorId(centroDistribucionId)
                .orElseThrow(() -> new IllegalArgumentException("El centro de distribución no existe"));

        authorizationService.requirePermisoCentroDistribucion(
                authUserId,
                centro,
                "consultar el inventario de este centro de distribución");

        List<Inventario> inventarios = inventarioRepositoryPort
                .findByCentroDistribucionId(centroDistribucionId);

        if (inventarios.isEmpty()) {
            return List.of();
        }

        // Optimización N+1: Carga batch de productos en una sola consulta SQL
        Set<Long> productoIds = inventarios.stream()
                .map(Inventario::getProductoId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Producto> productosMap = productoRepositoryPort.findAllById(productoIds).stream()
                .collect(Collectors.toMap(Producto::getId, p -> p));

        String nombreFiltro = (nombre != null && !nombre.isBlank()) ? nombre.trim().toLowerCase() : null;
        String catFiltro = (categoria != null && !categoria.isBlank()) ? categoria.trim().toLowerCase() : null;

        return inventarios.stream()
                .filter(i -> productoId == null || productoId.equals(i.getProductoId()))
                .filter(i -> {
                    Producto producto = productosMap.get(i.getProductoId());
                    if (producto == null) {
                        return nombreFiltro == null && catFiltro == null;
                    }
                    if (nombreFiltro != null && (producto.getNombre() == null 
                            || !producto.getNombre().toLowerCase().contains(nombreFiltro))) {
                        return false;
                    }
                    if (catFiltro != null && (producto.getCategoria() == null 
                            || !producto.getCategoria().toLowerCase().equalsIgnoreCase(catFiltro))) {
                        return false;
                    }
                    return true;
                })
                .map(i -> {
                    Producto producto = productosMap.get(i.getProductoId());
                    if (producto != null) {
                        i.setNombreProducto(producto.getNombre());
                        i.setCategoriaProducto(producto.getCategoria());
                    }
                    i.setStockBajo(
                            i.getCantidadActual() != null
                                    && i.getStockMinimo() != null
                                    && i.getCantidadActual() < i.getStockMinimo()
                    );
                    return i;
                })
                .toList();
    }
}
