
package com.example.optiway.application.service;

import com.example.optiway.application.port.in.ConsultarInventarioCDUseCase;
import com.example.optiway.application.port.out.CentroDistribucionRepositoryPort;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.domain.model.Inventario;
import com.example.optiway.domain.model.Producto;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ConsultarInventarioCDService
        implements ConsultarInventarioCDUseCase {

    private final InventarioRepositoryPort inventarioRepositoryPort;
    private final ProductoRepositoryPort productoRepositoryPort;
    private final CentroDistribucionRepositoryPort centroDistribucionRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public ConsultarInventarioCDService(
            InventarioRepositoryPort inventarioRepositoryPort,
            ProductoRepositoryPort productoRepositoryPort,
            CentroDistribucionRepositoryPort centroDistribucionRepositoryPort,
            UsuarioRepositoryPort usuarioRepositoryPort) {

        this.inventarioRepositoryPort = inventarioRepositoryPort;
        this.productoRepositoryPort = productoRepositoryPort;
        this.centroDistribucionRepositoryPort = centroDistribucionRepositoryPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    @Override
    public List<Inventario> consultarInventarioCD(
            UUID authUserId,
            Long centroDistribucionId,
            String nombre,
            String categoria,
            Long productoId) {

        Usuario usuario = usuarioRepositoryPort
                .obtenerPorAuthUserId(authUserId)
                .orElseThrow(() ->
                        new AccesoDenegadoException("Usuario no encontrado"));

        if (usuario.getRol() != Rol.ENCARGADO_CD) {
            throw new AccesoDenegadoException(
                    "Solo un encargado de CD puede consultar este inventario");
        }

        CentroDistribucion centro = centroDistribucionRepositoryPort
                .obtenerPorId(centroDistribucionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El centro de distribución no existe"));

        if (centro.getEncargadoId() == null
                || !usuario.getId().equals(centro.getEncargadoId())) {
            throw new AccesoDenegadoException(
                    "No tiene permisos para consultar el inventario de este centro de distribución");
        }

        return inventarioRepositoryPort
                .findByCentroDistribucionId(centroDistribucionId)
                .stream()
                .filter(i -> productoId == null
                        || productoId.equals(i.getProductoId()))
                .filter(i -> {
                    Producto producto = productoRepositoryPort
                            .findById(i.getProductoId())
                            .orElse(null);

                    if (producto == null) {
                        return nombre == null || nombre.isBlank();
                    }

                    return nombre == null
                            || producto.getNombre().toLowerCase()
                            .contains(nombre.toLowerCase());
                })
                .filter(i -> {
                    if (categoria == null) {
                        return true;
                    }

                    Producto producto = productoRepositoryPort
                            .findById(i.getProductoId())
                            .orElse(null);

                    return producto != null
                            && producto.getCategoria()
                            .equalsIgnoreCase(categoria);
                })
                .map(i -> {
                    Producto producto = productoRepositoryPort
                            .findById(i.getProductoId())
                            .orElse(null);

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
