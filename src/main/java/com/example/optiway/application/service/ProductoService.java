package com.example.optiway.application.service;

import com.example.optiway.application.port.in.CrearProductoUseCase;
import com.example.optiway.application.port.in.EditarProductoUseCase;
import com.example.optiway.application.port.in.ObtenerProductosUseCase;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.domain.model.Producto;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.Usuario;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ProductoService implements CrearProductoUseCase, ObtenerProductosUseCase, EditarProductoUseCase {

    private final ProductoRepositoryPort productoRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public ProductoService(ProductoRepositoryPort productoRepositoryPort,
                           UsuarioRepositoryPort usuarioRepositoryPort) {
        this.productoRepositoryPort = productoRepositoryPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    @Override
    public Producto crearProducto(Producto producto, UUID authUserId) {
        if (authUserId == null) {
            throw new AccesoDenegadoException("Solo un administrador puede crear productos");
        }
        Usuario usuario = usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)
                .orElseThrow(() -> new AccesoDenegadoException("Usuario no encontrado"));
        if (usuario.getRol() != Rol.ADMINISTRADOR) {
            throw new AccesoDenegadoException("Solo un administrador puede crear productos");
        }
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
        producto.setEstado("activo");
        return productoRepositoryPort.save(producto);
    }

    @Override
    public List<Producto> obtenerProductos() {
        return productoRepositoryPort.findAll();
    }

    @Override
    public Producto editarProducto(Long id, Producto cambios, UUID authUserId) {
        if (authUserId == null) {
            throw new AccesoDenegadoException("Solo un administrador puede editar productos");
        }
        Usuario usuario = usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)
                .orElseThrow(() -> new AccesoDenegadoException("Usuario no encontrado"));
        if (usuario.getRol() != Rol.ADMINISTRADOR) {
            throw new AccesoDenegadoException("Solo un administrador puede editar productos");
        }
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

    private String requerido(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor.trim();
    }
}
