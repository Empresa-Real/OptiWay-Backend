package com.example.optiway.application.service;

import com.example.optiway.application.port.in.ConsultarInventarioUseCase;
import com.example.optiway.application.port.out.CentroDistribucionRepositoryPort;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.domain.model.CentroDistribucion;
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
    private final CentroDistribucionRepositoryPort centroDistribucionRepositoryPort;
    private final AuthorizationService authorizationService;

    public InventarioService(
            InventarioRepositoryPort inventarioRepositoryPort,
            ProductoRepositoryPort productoRepositoryPort,
            TiendaRepositoryPort tiendaRepositoryPort,
            CentroDistribucionRepositoryPort centroDistribucionRepositoryPort,
            AuthorizationService authorizationService) {

        this.inventarioRepositoryPort = inventarioRepositoryPort;
        this.productoRepositoryPort = productoRepositoryPort;
        this.tiendaRepositoryPort = tiendaRepositoryPort;
        this.centroDistribucionRepositoryPort = centroDistribucionRepositoryPort;
        this.authorizationService = authorizationService;
    }

    @Override
    public List<Inventario> consultarInventario(
            UUID authUserId,
            Long tiendaId,
            Long centroDistribucionId,
            String nombre,
            String categoria,
            Long productoId) {

        validarAcceso(authUserId, tiendaId, centroDistribucionId);

        List<Inventario> inventarios = obtenerInventarios(tiendaId, centroDistribucionId, productoId);
        if (inventarios.isEmpty()) {
            return List.of();
        }

        Map<Long, Producto> productosMap = cargarProductosEnBatch(inventarios);

        String nombreFiltro = normalizar(nombre);
        String catFiltro = normalizar(categoria);

        return inventarios.stream()
                .filter(i -> coincideFiltros(productosMap.get(i.getProductoId()), nombreFiltro, catFiltro))
                .map(i -> i.enriquecerConProducto(productosMap.get(i.getProductoId())))
                .toList();
    }

    private void validarAcceso(UUID authUserId, Long tiendaId, Long centroDistribucionId) {
        if (tiendaId != null) {
            Tienda tienda = tiendaRepositoryPort.obtenerPorId(tiendaId)
                    .orElseThrow(() -> new IllegalArgumentException("La tienda no existe."));
            authorizationService.requirePermisoConsultarTienda(authUserId, tienda, "consultar esta tienda");
        } else if (centroDistribucionId != null) {
            CentroDistribucion centro = centroDistribucionRepositoryPort.obtenerPorId(centroDistribucionId)
                    .orElseThrow(() -> new IllegalArgumentException("El centro de distribución no existe"));
            authorizationService.requirePermisoCentroDistribucion(
                    authUserId, centro, "consultar el inventario de este centro de distribución");
        }
    }

    private List<Inventario> obtenerInventarios(Long tiendaId, Long centroDistribucionId, Long productoId) {
        if (tiendaId != null) {
            if (productoId != null) {
                return inventarioRepositoryPort.findByTiendaIdAndProductoId(tiendaId, productoId)
                        .map(List::of)
                        .orElseGet(List::of);
            }
            return inventarioRepositoryPort.findByTiendaId(tiendaId);
        } else if (centroDistribucionId != null) {
            if (productoId != null) {
                return inventarioRepositoryPort.findByCentroDistribucionIdAndProductoId(centroDistribucionId, productoId)
                        .map(List::of)
                        .orElseGet(List::of);
            }
            return inventarioRepositoryPort.findByCentroDistribucionId(centroDistribucionId);
        }
        return List.of();
    }

    private Map<Long, Producto> cargarProductosEnBatch(List<Inventario> inventarios) {
        Set<Long> productoIds = inventarios.stream()
                .map(Inventario::getProductoId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return productoRepositoryPort.findAllById(productoIds).stream()
                .collect(Collectors.toMap(Producto::getId, p -> p));
    }

    private boolean coincideFiltros(Producto producto, String nombreFiltro, String catFiltro) {
        if (producto == null) {
            return nombreFiltro == null && catFiltro == null;
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
    }

    private String normalizar(String valor) {
        return (valor != null && !valor.isBlank()) ? valor.trim().toLowerCase() : null;
    }
}
