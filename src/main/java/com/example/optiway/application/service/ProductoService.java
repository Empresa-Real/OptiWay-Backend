package com.example.optiway.application.service;

import com.example.optiway.application.exception.ProductoDuplicadoException;
import com.example.optiway.application.port.in.CambiarEstadoProductoCommand;
import com.example.optiway.application.port.in.CambiarEstadoProductoUseCase;
import com.example.optiway.application.port.in.CrearProductoCommand;
import com.example.optiway.application.port.in.CrearProductoUseCase;
import com.example.optiway.application.port.in.EditarProductoCommand;
import com.example.optiway.application.port.in.EditarProductoUseCase;
import com.example.optiway.application.port.in.ObtenerProductosUseCase;
import com.example.optiway.application.port.in.VerificarStockProductoUseCase;
import com.example.optiway.application.port.in.dto.ProductoResponse;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.domain.model.Producto;
import com.example.optiway.domain.model.Usuario;
import com.example.optiway.application.port.out.AuthorizationPort;
import com.example.optiway.infrastructure.security.AuthorizedAction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ProductoService implements CrearProductoUseCase, ObtenerProductosUseCase, EditarProductoUseCase, CambiarEstadoProductoUseCase, VerificarStockProductoUseCase {

    private final ProductoRepositoryPort productoRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final InventarioRepositoryPort inventarioRepositoryPort;
    private final AuthorizationPort authorizationService;

    public ProductoService(ProductoRepositoryPort productoRepositoryPort,
                           UsuarioRepositoryPort usuarioRepositoryPort,
                           InventarioRepositoryPort inventarioRepositoryPort,
                           AuthorizationPort authorizationService) {
        this.productoRepositoryPort = productoRepositoryPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.inventarioRepositoryPort = inventarioRepositoryPort;
        this.authorizationService = authorizationService;
    }

    @Override
    public ProductoResponse crearProducto(UUID authUserId, CrearProductoCommand command) {
        authorizationService.requireGestionLogistica(authUserId, AuthorizedAction.CREAR_PRODUCTO);

        String identificador = requerido(command.identificador(), "El identificador es obligatorio.")
                .toUpperCase(Locale.ROOT);
        String nombre = requerido(command.nombre(), "El nombre es obligatorio.");
        String categoria = requerido(command.categoria(), "La categoría es obligatoria.");

        if (command.precio() != null && command.precio().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
        String unidadMedida = null;
        if (command.unidadMedida() != null && !command.unidadMedida().isBlank()) {
            unidadMedida = command.unidadMedida().trim();
        }

        if (productoRepositoryPort.existsByIdentificador(identificador)) {
            throw new ProductoDuplicadoException("Ya existe un producto con ese identificador.");
        }

        Producto producto = new Producto();
        producto.setIdentificador(identificador);
        producto.setNombre(nombre);
        producto.setCategoria(categoria);
        producto.setPrecio(command.precio());
        producto.setUnidadMedida(unidadMedida);
        producto.setActivo(true);
        return ProductoResponse.from(productoRepositoryPort.save(producto));
    }

    @Override
    public List<ProductoResponse> obtenerProductos(UUID authUserId) {
        authorizationService.requireAuthenticated(authUserId);
        return productoRepositoryPort.findAll().stream()
                .map(ProductoResponse::from)
                .toList();
    }

    @Override
    public ProductoResponse editarProducto(UUID authUserId, EditarProductoCommand command) {
        Usuario usuario = authorizationService.requireGestionLogistica(authUserId, AuthorizedAction.EDITAR_PRODUCTO);
        if (command.id() == null || command.id() <= 0) {
            throw new IllegalArgumentException("El ID del producto no es válido.");
        }

        Producto actual = productoRepositoryPort.findById(command.id())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + command.id()));

        boolean hayCambios = false;
        if (command.nombre() != null) {
            actual.setNombre(requerido(command.nombre(), "El nombre es obligatorio."));
            hayCambios = true;
        }
        if (command.categoria() != null) {
            actual.setCategoria(requerido(command.categoria(), "La categoría es obligatoria."));
            hayCambios = true;
        }
        if (command.precio() != null) {
            if (command.precio().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("El precio debe ser mayor que cero.");
            }
            actual.setPrecio(command.precio());
            hayCambios = true;
        }
        if (command.unidadMedida() != null) {
            actual.setUnidadMedida(requerido(command.unidadMedida(), "La unidad de medida es obligatoria."));
            hayCambios = true;
        }
        if (!hayCambios) {
            throw new IllegalArgumentException("Debe indicar al menos un campo para editar.");
        }

        // ID, identificador y estado se toman del registro actual y nunca del cuerpo de la solicitud.
        return ProductoResponse.from(productoRepositoryPort.update(actual, usuario.getId()));
    }

    @Override
    public ProductoResponse cambiarEstadoProducto(UUID authUserId, CambiarEstadoProductoCommand command) {
        authorizationService.requireGestionLogistica(authUserId, AuthorizedAction.CAMBIAR_ESTADO_PRODUCTO);
        if (command.id() == null || command.id() <= 0) {
            throw new IllegalArgumentException("El ID del producto no es válido.");
        }

        Producto producto = productoRepositoryPort.findById(command.id())
                .orElseThrow(() -> new IllegalArgumentException("El producto no existe con ID: " + command.id()));

        producto.setActivo(command.activo());
        return ProductoResponse.from(productoRepositoryPort.save(producto));
    }

    @Override
    public boolean tieneStockActivo(UUID authUserId, Long productoId) {
        authorizationService.requireAuthenticated(authUserId);
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
