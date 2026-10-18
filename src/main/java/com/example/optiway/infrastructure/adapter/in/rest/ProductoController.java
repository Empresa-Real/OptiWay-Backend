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
import com.example.optiway.infrastructure.security.AuthUserId;
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
import java.util.Map;
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
    public Producto crearProducto(@RequestBody CrearProductoRequest request, @AuthUserId UUID authUserId) {
        Producto producto = new Producto();
        producto.setIdentificador(request.getIdentificador());
        producto.setNombre(request.getNombre());
        producto.setCategoria(request.getCategoria());
        producto.setPrecio(request.getPrecio());
        producto.setUnidadMedida(request.getUnidadMedida());
        return crearProductoUseCase.crearProducto(producto, authUserId);
    }

    @GetMapping
    public List<Producto> obtenerProductos() {
        return obtenerProductosUseCase.obtenerProductos();
    }

    @PatchMapping("/{id}")
    public Producto editarProducto(@PathVariable Long id,
                                   @RequestBody EditarProductoRequest request,
                                   @AuthUserId UUID authUserId) {
        Producto cambios = new Producto();
        cambios.setNombre(request.getNombre());
        cambios.setCategoria(request.getCategoria());
        cambios.setPrecio(request.getPrecio());
        cambios.setUnidadMedida(request.getUnidadMedida());
        return editarProductoUseCase.editarProducto(id, cambios, authUserId);
    }

    @PatchMapping("/{id}/estado")
    public Producto cambiarEstado(@PathVariable Long id,
                                  @RequestBody CambiarEstadoProductoRequest request,
                                  @AuthUserId UUID authUserId) {
        if (request == null || request.getActivo() == null) {
            throw new IllegalArgumentException("Debe indicar si el producto estará activo o inactivo");
        }
        return cambiarEstadoProductoUseCase.cambiarEstadoProducto(authUserId, id, request.getActivo());
    }

    @GetMapping("/{id}/tiene-stock")
    public Map<String, Boolean> tieneStock(@PathVariable Long id) {
        return Map.of("tieneStock", verificarStockProductoUseCase.tieneStockActivo(id));
    }
}
