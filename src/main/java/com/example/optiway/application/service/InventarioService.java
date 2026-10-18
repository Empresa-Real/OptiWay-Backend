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

        validarAccesoATienda(authUserId, tiendaId);

        List<Inventario> inventarios = obtenerInventarios(tiendaId, productoId);
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

    private void validarAccesoATienda(UUID authUserId, Long tiendaId) {
        Tienda tienda = tiendaRepositoryPort.obtenerPorId(tiendaId)
                .orElseThrow(() -> new AccesoDenegadoException("La tienda no existe"));

        authorizationService.requirePermisoConsultarTienda(authUserId, tienda, "consultar esta tienda");
    }

    private List<Inventario> obtenerInventarios(Long tiendaId, Long productoId) {
        if (productoId != null) {
            return inventarioRepositoryPort.findByTiendaIdAndProductoId(tiendaId, productoId)
                    .map(List::of)
                    .orElseGet(List::of);
        }
        return inventarioRepositoryPort.findByTiendaId(tiendaId);
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
