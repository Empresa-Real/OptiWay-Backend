package com.example.optiway.infrastructure.adapter.in.rest;

import java.util.UUID;

import com.example.optiway.application.port.in.AjustarInventarioCDUseCase;
import com.example.optiway.application.port.in.dto.InventarioResponse;
import com.example.optiway.infrastructure.adapter.in.rest.dto.AjustarInventarioCDRequest;
import com.example.optiway.infrastructure.security.AuthUserId;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cd")
public class InventarioCDAliasController {
    private final AjustarInventarioCDUseCase ajustarInventarioCDUseCase;

    public InventarioCDAliasController(AjustarInventarioCDUseCase ajustarInventarioCDUseCase) {
        this.ajustarInventarioCDUseCase = ajustarInventarioCDUseCase;
    }

    @PatchMapping("/{centroDistribucionId}/inventario")
    public InventarioResponse ajustarInventario(
            @AuthUserId UUID authUserId,
            @PathVariable Long centroDistribucionId,
            @Valid @RequestBody AjustarInventarioCDRequest request) {
        return ajustarInventarioCDUseCase.ajustarInventario(
                authUserId, centroDistribucionId, request.productoId(), request.nuevaCantidad());
    }
}
