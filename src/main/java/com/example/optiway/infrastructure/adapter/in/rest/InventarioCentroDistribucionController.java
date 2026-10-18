package com.example.optiway.infrastructure.adapter.in.rest;

import com.example.optiway.application.port.in.ConsultarInventarioCDUseCase;
import com.example.optiway.domain.model.Inventario;
import com.example.optiway.infrastructure.security.AuthUserId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/inventario/centro-distribucion", "/api/inventario/cd"})
public class InventarioCentroDistribucionController {

    private final ConsultarInventarioCDUseCase consultarInventarioCDUseCase;

    public InventarioCentroDistribucionController(
            ConsultarInventarioCDUseCase consultarInventarioCDUseCase) {
        this.consultarInventarioCDUseCase = consultarInventarioCDUseCase;
    }

    @GetMapping
    public List<Inventario> consultarInventarioCD(
            @AuthUserId UUID authUserId,
            @RequestParam Long centroDistribucionId,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Long productoId) {

        return consultarInventarioCDUseCase.consultarInventarioCD(
                authUserId,
                centroDistribucionId,
                nombre,
                categoria,
                productoId
        );
    }
}
