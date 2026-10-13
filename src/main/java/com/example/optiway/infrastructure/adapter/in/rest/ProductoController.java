
package com.example.optiway.infrastructure.adapter.in.rest;

import com.example.optiway.application.port.in.CambiarEstadoProductoUseCase;
import com.example.optiway.application.port.in.CambioEstadoProductoResultado;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final CambiarEstadoProductoUseCase cambiarEstadoProductoUseCase;

    public ProductoController(
            CambiarEstadoProductoUseCase cambiarEstadoProductoUseCase) {
        this.cambiarEstadoProductoUseCase = cambiarEstadoProductoUseCase;
    }

    @PatchMapping("/{productoId}/estado")
    public CambioEstadoProductoResultado cambiarEstado(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productoId,
            @RequestBody CambiarEstadoProductoRequest request) {

        if (request.getActivo() == null) {
            throw new IllegalArgumentException(
                    "Debe indicar si el producto estará activo o inactivo");
        }

        boolean confirmar = Boolean.TRUE.equals(request.getConfirmar());

        return cambiarEstadoProductoUseCase.cambiarEstadoProducto(
                UUID.fromString(jwt.getSubject()),
                productoId,
                request.getActivo(),
                confirmar
        );
    }
}

