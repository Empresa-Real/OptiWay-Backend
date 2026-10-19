package com.example.optiway.infrastructure.adapter.in.rest;

import java.util.UUID;

import com.example.optiway.application.port.in.RegistrarIngresoCommand;
import com.example.optiway.application.port.in.RegistrarIngresoMercanciaUseCase;
import com.example.optiway.application.port.in.dto.IngresoMercanciaResponse;
import com.example.optiway.infrastructure.adapter.in.rest.dto.RegistrarIngresoMercanciaRequest;
import com.example.optiway.infrastructure.security.AuthUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ingresos-mercancia")
@Tag(name = "Ingresos de Mercancía", description = "Endpoints para el registro directo de entradas de stock a centros de distribución")
public class IngresoMercanciaController {

    private final RegistrarIngresoMercanciaUseCase registrarIngresoMercanciaUseCase;

    public IngresoMercanciaController(
            RegistrarIngresoMercanciaUseCase registrarIngresoMercanciaUseCase) {
        this.registrarIngresoMercanciaUseCase = registrarIngresoMercanciaUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar ingreso de mercancía", description = "Registra una entrada de inventario a un CD indicando producto, cantidad y procedencia.")
    public IngresoMercanciaResponse registrarIngreso(
            @Valid @RequestBody RegistrarIngresoMercanciaRequest request,
            @AuthUserId UUID authUserId) {

        RegistrarIngresoCommand command = new RegistrarIngresoCommand(
                request.getCentroDistribucionId(),
                request.getProductoId(),
                request.getCantidad(),
                request.getOrigen()
        );

        return IngresoMercanciaResponse.from(registrarIngresoMercanciaUseCase.registrarIngreso(command, authUserId));
    }
}