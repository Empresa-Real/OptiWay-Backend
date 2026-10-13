
package com.example.optiway.infrastructure.adapter.in.rest;

import com.example.optiway.application.port.in.ConsultarInventarioCDUseCase;
import com.example.optiway.domain.model.Inventario;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventario/centro-distribucion")
public class InventarioCentroDistribucionController {

    private final ConsultarInventarioCDUseCase consultarInventarioCDUseCase;

    public InventarioCentroDistribucionController(
            ConsultarInventarioCDUseCase consultarInventarioCDUseCase) {
        this.consultarInventarioCDUseCase = consultarInventarioCDUseCase;
    }

    @GetMapping
    public List<Inventario> consultarInventarioCD(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Long centroDistribucionId,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Long productoId) {

        return consultarInventarioCDUseCase.consultarInventarioCD(
                UUID.fromString(jwt.getSubject()),
                centroDistribucionId,
                nombre,
                categoria,
                productoId
        );
    }
}
