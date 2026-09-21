package com.example.optiway.application.service;

import com.example.optiway.application.port.in.ConsultarInventarioUseCase;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.domain.model.Inventario;
import com.example.optiway.domain.model.Producto;
import com.example.optiway.domain.model.Tienda;
import com.example.optiway.domain.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class InventarioService implements ConsultarInventarioUseCase {

    private final InventarioRepositoryPort inventarioRepositoryPort;
    private final ProductoRepositoryPort productoRepositoryPort;
    private final TiendaRepositoryPort tiendaRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public InventarioService(
            InventarioRepositoryPort inventarioRepositoryPort,
            ProductoRepositoryPort productoRepositoryPort,
            TiendaRepositoryPort tiendaRepositoryPort,
            UsuarioRepositoryPort usuarioRepositoryPort) {

        this.inventarioRepositoryPort = inventarioRepositoryPort;
        this.productoRepositoryPort = productoRepositoryPort;
        this.tiendaRepositoryPort = tiendaRepositoryPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    @Override
    public List<Inventario> consultarInventario(
            UUID authUserId,
            Long tiendaId,
            String nombre,
            String categoria,
            Long productoId) {

        Usuario usuario = usuarioRepositoryPort
                .obtenerPorAuthUserId(authUserId)
                .orElseThrow(() ->
                        new AccesoDenegadoException(
                                "Usuario no encontrado"));

        Tienda tienda = tiendaRepositoryPort
                .obtenerPorId(tiendaId)
                .orElseThrow(() ->
                        new AccesoDenegadoException(
                                "La tienda no existe"));

        if (!usuario.getId().equals(tienda.getEncargadoId())) {
            throw new AccesoDenegadoException(
                    "No tiene permisos para consultar esta tienda");
        }

        List<Inventario> inventarios =
                inventarioRepositoryPort.findByTiendaId(tiendaId);

        return inventarios.stream()
                .filter(inventario -> {

                    if (productoId != null &&
                            !inventario.getProductoId().equals(productoId)) {
                        return false;
                    }

                    Producto producto = productoRepositoryPort
                            .findById(inventario.getProductoId())
                            .orElse(null);

                    if (producto == null) {
                        return false;
                    }

                    if (nombre != null && !nombre.isBlank() &&
                            !producto.getNombre()
                                    .toLowerCase()
                                    .contains(nombre.toLowerCase())) {
                        return false;
                    }

                    if (categoria != null && !categoria.isBlank() &&
                            !producto.getCategoria()
                                    .equalsIgnoreCase(categoria)) {
                        return false;
                    }

                    return true;
                })
                .map(inventario -> {

                    Producto producto = productoRepositoryPort
                            .findById(inventario.getProductoId())
                            .orElse(null);

                    if (producto != null) {
                        inventario.setNombreProducto(producto.getNombre());
                        inventario.setCategoriaProducto(
                                producto.getCategoria());
                    }

                    inventario.setStockBajo(
                            inventario.getCantidadActual()
                                    < inventario.getStockMinimo());

                    return inventario;
                })
                .toList();
    }
}
