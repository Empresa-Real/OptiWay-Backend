
package com.example.optiway.application.service;

import com.example.optiway.application.port.in.CambiarEstadoProductoUseCase;
import com.example.optiway.application.port.in.CambioEstadoProductoResultado;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.domain.model.Producto;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProductoService implements CambiarEstadoProductoUseCase {

    private final ProductoRepositoryPort productoRepositoryPort;
    private final InventarioRepositoryPort inventarioRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public ProductoService(
            ProductoRepositoryPort productoRepositoryPort,
            InventarioRepositoryPort inventarioRepositoryPort,
            UsuarioRepositoryPort usuarioRepositoryPort) {
        this.productoRepositoryPort = productoRepositoryPort;
        this.inventarioRepositoryPort = inventarioRepositoryPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    @Override
    public CambioEstadoProductoResultado cambiarEstadoProducto(
            UUID authUserId,
            Long productoId,
            boolean activoSolicitado,
            boolean confirmar) {

        Usuario usuario = usuarioRepositoryPort
                .obtenerPorAuthUserId(authUserId)
                .orElseThrow(() ->
                        new AccesoDenegadoException("Usuario no encontrado"));

        if (usuario.getRol() != Rol.ADMINISTRADOR) {
            throw new AccesoDenegadoException(
                    "Solo un administrador puede activar o desactivar productos");
        }

        Producto producto = productoRepositoryPort.findById(productoId)
                .orElseThrow(() ->
                        new IllegalArgumentException("El producto no existe"));

        boolean tieneExistencias =
                inventarioRepositoryPort.existeStockActivoDeProducto(productoId);

        if (!activoSolicitado && producto.isActivo()
                && tieneExistencias && !confirmar) {

            return new CambioEstadoProductoResultado(
                    true,
                    true,
                    true,
                    "El producto tiene existencias en inventario. "
                            + "Confirme la desactivación o cancele la operación."
            );
        }

        producto.setActivo(activoSolicitado);
        productoRepositoryPort.save(producto);

        String mensaje = activoSolicitado
                ? "Producto reactivado correctamente."
                : "Producto desactivado correctamente. "
                + "Su registro se conserva en el sistema.";

        return new CambioEstadoProductoResultado(
                producto.isActivo(),
                false,
                tieneExistencias,
                mensaje
        );
    }
}
