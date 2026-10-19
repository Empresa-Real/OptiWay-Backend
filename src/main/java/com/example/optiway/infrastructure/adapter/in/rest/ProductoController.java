package com.example.optiway.infrastructure.adapter.in.rest;

import com.example.optiway.application.port.in.CambiarEstadoProductoUseCase;
import com.example.optiway.application.port.in.CrearProductoUseCase;
import com.example.optiway.application.port.in.EditarProductoUseCase;
import com.example.optiway.application.port.in.ObtenerProductosUseCase;
import com.example.optiway.application.port.in.VerificarStockProductoUseCase;
import com.example.optiway.application.port.in.dto.ProductoResponse;
import com.example.optiway.application.port.in.dto.ProductoStockResponse;
import com.example.optiway.domain.model.Producto;
import com.example.optiway.infrastructure.adapter.in.rest.dto.CambiarEstadoProductoRequest;
import com.example.optiway.infrastructure.adapter.in.rest.dto.CrearProductoRequest;
import com.example.optiway.infrastructure.adapter.in.rest.dto.EditarProductoRequest;
import com.example.optiway.infrastructure.security.AuthUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Endpoints para creación, edición, consulta y cambio de estado de productos en el catálogo")
public class ProductoController {

    private final CrearProductoUseCase crearProductoUseCase;
    private final ObtenerProductosUseCase obtenerProductosUseCase;
    private final EditarProductoUseCase editarProductoUseCase;
    private final CambiarEstadoProductoUseCase cambiarEstadoProductoUseCase;
    private final VerificarStockProductoUseCase verificarStockProductoUseCase;

    public ProductoController(CrearProductoUseCase crearProductoUseCase,
                              ObtenerProductosUseCase obtenerProductosUseCase,
                              EditarProductoUseCase editarProductoUseCase,
                              CambiarEstadoProductoUseCase cambiarEstadoProductoUseCase,
                              VerificarStockProductoUseCase verificarStockProductoUseCase) {
        this.crearProductoUseCase = crearProductoUseCase;
        this.obtenerProductosUseCase = obtenerProductosUseCase;
        this.editarProductoUseCase = editarProductoUseCase;
        this.cambiarEstadoProductoUseCase = cambiarEstadoProductoUseCase;
        this.verificarStockProductoUseCase = verificarStockProductoUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear nuevo producto", description = "Registra un producto en el catálogo. Requiere rol ADMINISTRADOR.")
    public ProductoResponse crearProducto(@Valid @RequestBody CrearProductoRequest request, @AuthUserId UUID authUserId) {
        Producto producto = crearProductoUseCase.crearProducto(authUserId, request.toDomain());
        return ProductoResponse.from(producto);
    }

    @GetMapping
    @Operation(summary = "Listar catálogo de productos", description = "Retorna todos los productos registrados en el sistema.")
    public List<ProductoResponse> obtenerProductos() {
        return obtenerProductosUseCase.obtenerProductos().stream()
                .map(ProductoResponse::from)
                .toList();
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Editar producto existente", description = "Modifica los atributos comerciales de un producto. Requiere rol ADMINISTRADOR.")
    public ProductoResponse editarProducto(@PathVariable Long id,
                                           @Valid @RequestBody EditarProductoRequest request,
                                           @AuthUserId UUID authUserId) {
        Producto producto = editarProductoUseCase.editarProducto(authUserId, id, request.toDomain());
        return ProductoResponse.from(producto);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado de producto", description = "Activa o desactiva un producto en el catálogo. Requiere rol ADMINISTRADOR.")
    public ProductoResponse cambiarEstado(@PathVariable Long id,
                                          @Valid @RequestBody CambiarEstadoProductoRequest request,
                                          @AuthUserId UUID authUserId) {
        Producto producto = cambiarEstadoProductoUseCase.cambiarEstadoProducto(authUserId, id, request.activo());
        return ProductoResponse.from(producto);
    }

    @GetMapping("/{id}/tiene-stock")
    @Operation(summary = "Verificar existencias de producto", description = "Consulta si el producto cuenta con existencias activas en alguna tienda o centro de distribución.")
    public ProductoStockResponse tieneStock(@PathVariable Long id) {
        return new ProductoStockResponse(verificarStockProductoUseCase.tieneStockActivo(id));
    }
}
