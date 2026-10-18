package com.example.optiway.infrastructure.adapter.in.rest;

import com.example.optiway.application.port.in.CrearProductoUseCase;
import com.example.optiway.application.port.in.EditarProductoUseCase;
import com.example.optiway.application.port.in.ObtenerProductosUseCase;
import com.example.optiway.domain.model.Producto;
import com.example.optiway.infrastructure.adapter.in.rest.dto.CrearProductoRequest;
import com.example.optiway.infrastructure.adapter.in.rest.dto.EditarProductoRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

    public ProductoController(CrearProductoUseCase crearProductoUseCase,
                              ObtenerProductosUseCase obtenerProductosUseCase,
                              EditarProductoUseCase editarProductoUseCase) {
        this.crearProductoUseCase = crearProductoUseCase;
        this.obtenerProductosUseCase = obtenerProductosUseCase;
        this.editarProductoUseCase = editarProductoUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Producto crearProducto(@RequestBody CrearProductoRequest request, @AuthenticationPrincipal Jwt jwt) {
        Producto producto = new Producto();
        producto.setIdentificador(request.getIdentificador());
        producto.setNombre(request.getNombre());
        producto.setCategoria(request.getCategoria());
        producto.setPrecio(request.getPrecio());
        producto.setUnidadMedida(request.getUnidadMedida());
        return crearProductoUseCase.crearProducto(producto, extraerAuthUserId(jwt));
    }

    @GetMapping
    public List<Producto> obtenerProductos() {
        return obtenerProductosUseCase.obtenerProductos();
    }

    @PatchMapping("/{id}")
    public Producto editarProducto(@PathVariable Long id,
                                   @RequestBody EditarProductoRequest request,
                                   @AuthenticationPrincipal Jwt jwt) {
        Producto cambios = new Producto();
        cambios.setNombre(request.getNombre());
        cambios.setCategoria(request.getCategoria());
        cambios.setPrecio(request.getPrecio());
        cambios.setUnidadMedida(request.getUnidadMedida());
        return editarProductoUseCase.editarProducto(id, cambios, extraerAuthUserId(jwt));
    }

    private UUID extraerAuthUserId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null) {
            return null;
        }
        try {
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
