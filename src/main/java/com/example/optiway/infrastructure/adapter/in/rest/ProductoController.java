package com.example.optiway.infrastructure.adapter.in.rest;

import com.example.optiway.application.port.in.CambiarEstadoProductoUseCase;
import com.example.optiway.application.port.in.CrearProductoUseCase;
import com.example.optiway.application.port.in.EditarProductoUseCase;
import com.example.optiway.application.port.in.ObtenerProductosUseCase;
import com.example.optiway.application.port.in.VerificarStockProductoUseCase;
import com.example.optiway.domain.model.Producto;
import com.example.optiway.infrastructure.adapter.in.rest.dto.CambiarEstadoProductoRequest;
import com.example.optiway.infrastructure.adapter.in.rest.dto.CrearProductoRequest;
import com.example.optiway.infrastructure.adapter.in.rest.dto.EditarProductoRequest;
import com.example.optiway.infrastructure.adapter.in.rest.dto.ProductoStockResponse;
import com.example.optiway.infrastructure.security.AuthUserId;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
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
    public Producto crearProducto(@Valid @RequestBody CrearProductoRequest request, @AuthUserId UUID authUserId) {
        return crearProductoUseCase.crearProducto(request.toDomain(), authUserId);
    }

    @GetMapping
    public List<Producto> obtenerProductos() {
        return obtenerProductosUseCase.obtenerProductos();
    }

    @PatchMapping("/{id}")
    public Producto editarProducto(@PathVariable Long id,
                                   @Valid @RequestBody EditarProductoRequest request,
                                   @AuthUserId UUID authUserId) {
        return editarProductoUseCase.editarProducto(id, request.toDomain(), authUserId);
    }

    @PatchMapping("/{id}/estado")
    public Producto cambiarEstado(@PathVariable Long id,
                                  @Valid @RequestBody CambiarEstadoProductoRequest request,
                                  @AuthUserId UUID authUserId) {
        if (request == null || request.activo() == null) {
            throw new IllegalArgumentException("Debe indicar si el producto estará activo o inactivo");
        }
        return cambiarEstadoProductoUseCase.cambiarEstadoProducto(authUserId, id, request.activo());
    }

    @GetMapping("/{id}/tiene-stock")
    public ProductoStockResponse tieneStock(@PathVariable Long id) {
        return new ProductoStockResponse(verificarStockProductoUseCase.tieneStockActivo(id));
    }
}
