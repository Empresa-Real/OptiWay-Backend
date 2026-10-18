package com.example.optiway.application.service;

import com.example.optiway.application.port.in.ConsultarInventarioUseCase;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.domain.model.Inventario;
import com.example.optiway.domain.model.Producto;
import com.example.optiway.domain.model.Tienda;
import com.example.optiway.infrastructure.security.AuthorizationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InventarioService implements ConsultarInventarioUseCase {

    private final InventarioRepositoryPort inventarioRepositoryPort;
    private final ProductoRepositoryPort productoRepositoryPort;
    private final TiendaRepositoryPort tiendaRepositoryPort;
    private final AuthorizationService authorizationService;

    public InventarioService(
            InventarioRepositoryPort inventarioRepositoryPort,
            ProductoRepositoryPort productoRepositoryPort,
            TiendaRepositoryPort tiendaRepositoryPort,
            AuthorizationService authorizationService) {

        this.inventarioRepositoryPort = inventarioRepositoryPort;
        this.productoRepositoryPort = productoRepositoryPort;
        this.tiendaRepositoryPort = tiendaRepositoryPort;
        this.authorizationService = authorizationService;
    }

    @Override
    public List<Inventario> consultarInventario(
            UUID authUserId,
            Long tiendaId,
            String nombre,
            String categoria,
            Long productoId) {

        Tienda tienda = tiendaRepositoryPort
                .obtenerPorId(tiendaId)
                .orElseThrow(() -> new AccesoDenegadoException("La tienda no existe"));

        authorizationService.requirePermisoConsultarTienda(authUserId, tienda, "consultar esta tienda");

        List<Inventario> inventarios;
        if (productoId != null) {
            inventarios = inventarioRepositoryPort.findByTiendaIdAndProductoId(tiendaId, productoId)
                    .map(List::of)
                    .orElseGet(List::of);
        } else {
            inventarios = inventarioRepositoryPort.findByTiendaId(tiendaId);
        }

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
                .filter(inventario -> {
                    Producto producto = productosMap.get(inventario.getProductoId());
                    if (producto == null) {
                        return false;
                    }
                    if (nombreFiltro != null && (producto.getNombre() == null
                            || !producto.getNombre().toLowerCase().contains(nombreFiltro))) {
                        return false;
                    }
                    if (catFiltro != null && (producto.getCategoria() == null
                            || !producto.getCategoria().equalsIgnoreCase(catFiltro))) {
                        return false;
                    }
                    return true;
                })
                .map(inventario -> {
                    Producto producto = productosMap.get(inventario.getProductoId());
                    if (producto != null) {
                        inventario.setNombreProducto(producto.getNombre());
                        inventario.setCategoriaProducto(producto.getCategoria());
                    }
                    int stockMin = inventario.getStockMinimo() != null ? inventario.getStockMinimo() : 0;
                    int cantActual = inventario.getCantidadActual() != null ? inventario.getCantidadActual() : 0;
                    inventario.setStockBajo(cantActual < stockMin);
                    return inventario;
                })
                .toList();
    }
}
