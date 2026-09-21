package com.example.optiway.infraestructure.adapter.in.rest;

import com.example.optiway.application.port.in.ConsultarInventarioUseCase;
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
@RequestMapping("/api/inventario")
public class InventarioController {

    private final ConsultarInventarioUseCase consultarInventarioUseCase;

    public InventarioController(
            ConsultarInventarioUseCase consultarInventarioUseCase) {

        this.consultarInventarioUseCase = consultarInventarioUseCase;
    }

    @GetMapping
    public List<Inventario> consultarInventario(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Long tiendaId,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Long productoId) {

        return consultarInventarioUseCase.consultarInventario(
                UUID.fromString(jwt.getSubject()),
                tiendaId,
                nombre,
                categoria,
                productoId);
    }
}