package com.example.optiway.application.service;

import com.example.optiway.application.port.in.CambiarEstadoProductoUseCase;
import com.example.optiway.application.port.in.CrearProductoUseCase;
import com.example.optiway.application.port.in.EditarProductoUseCase;
import com.example.optiway.application.port.in.ObtenerProductosUseCase;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.domain.model.Producto;
import com.example.optiway.infrastructure.security.AuthorizationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ProductoService implements CrearProductoUseCase, ObtenerProductosUseCase, EditarProductoUseCase, CambiarEstadoProductoUseCase {

    private final ProductoRepositoryPort productoRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final InventarioRepositoryPort inventarioRepositoryPort;
    private final AuthorizationService authorizationService;

    public ProductoService(ProductoRepositoryPort productoRepositoryPort,
                           UsuarioRepositoryPort usuarioRepositoryPort,
                           InventarioRepositoryPort inventarioRepositoryPort,
                           AuthorizationService authorizationService) {
        this.productoRepositoryPort = productoRepositoryPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.inventarioRepositoryPort = inventarioRepositoryPort;
        this.authorizationService = authorizationService;
    }

    @Override
    public Producto crearProducto(Producto producto, UUID authUserId) {
        authorizationService.requireAdministrador(authUserId, "crear productos");
        if (producto == null) {
            throw new IllegalArgumentException("Los datos del producto son obligatorios.");
        }

        producto.setIdentificador(requerido(producto.getIdentificador(), "El identificador es obligatorio.")
                .toUpperCase(Locale.ROOT));
        producto.setNombre(requerido(producto.getNombre(), "El nombre es obligatorio."));
        producto.setCategoria(requerido(producto.getCategoria(), "La categoría es obligatoria."));

        if (producto.getPrecio() != null && producto.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
        if (producto.getUnidadMedida() != null && producto.getUnidadMedida().isBlank()) {
            producto.setUnidadMedida(null);
        } else if (producto.getUnidadMedida() != null) {
            producto.setUnidadMedida(producto.getUnidadMedida().trim());
        }

        if (productoRepositoryPort.existsByIdentificador(producto.getIdentificador())) {
            throw new ProductoDuplicadoException("Ya existe un producto con ese identificador.");
        }
        producto.setActivo(true);
        return productoRepositoryPort.save(producto);
    }

    @Override
    public List<Producto> obtenerProductos() {
        return productoRepositoryPort.findAll();
    }

    @Override
    public Producto editarProducto(Long id, Producto cambios, UUID authUserId) {
        authorizationService.requireAdministrador(authUserId, "editar productos");
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID del producto no es válido.");
        }
        if (cambios == null) {
            throw new IllegalArgumentException("Los datos de edición son obligatorios.");
        }

        Producto actual = productoRepositoryPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));
        boolean hayCambios = false;
        if (cambios.getNombre() != null) {
            actual.setNombre(requerido(cambios.getNombre(), "El nombre es obligatorio."));
            hayCambios = true;
        }
        if (cambios.getCategoria() != null) {
            actual.setCategoria(requerido(cambios.getCategoria(), "La categoría es obligatoria."));
            hayCambios = true;
        }
        if (cambios.getPrecio() != null) {
            if (cambios.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("El precio debe ser mayor que cero.");
            }
            actual.setPrecio(cambios.getPrecio());
            hayCambios = true;
        }
        if (cambios.getUnidadMedida() != null) {
            actual.setUnidadMedida(requerido(cambios.getUnidadMedida(), "La unidad de medida es obligatoria."));
            hayCambios = true;
        }
        if (!hayCambios) {
            throw new IllegalArgumentException("Debe indicar al menos un campo para editar.");
        }

        // ID, identificador y estado se toman del registro actual y nunca del cuerpo de la solicitud.
        return productoRepositoryPort.update(actual, authUserId);
    }

    @Override
    public Producto cambiarEstadoProducto(UUID authUserId, Long productoId, boolean activo) {
        authorizationService.requireAdministrador(authUserId, "activar o desactivar productos");
        if (productoId == null || productoId <= 0) {
            throw new IllegalArgumentException("El ID del producto no es válido.");
        }

        Producto producto = productoRepositoryPort.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("El producto no existe con ID: " + productoId));

        producto.setActivo(activo);
        return productoRepositoryPort.save(producto);
    }

    @Override
    public boolean tieneStockActivo(Long productoId) {
        if (productoId == null) {
            return false;
        }
        return inventarioRepositoryPort.existeStockActivoDeProducto(productoId);
    }

    private String requerido(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor.trim();
    }
}
