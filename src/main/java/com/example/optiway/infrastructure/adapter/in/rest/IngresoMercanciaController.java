package com.example.optiway.infrastructure.adapter.in.rest;

import java.util.UUID;

import com.example.optiway.infrastructure.security.AuthUserId;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.optiway.application.port.in.RegistrarIngresoMercanciaUseCase;

@RestController
@RequestMapping("/api/ingresos-mercancia")
public class IngresoMercanciaController {

    private final RegistrarIngresoMercanciaUseCase registrarIngresoMercanciaUseCase;

    public IngresoMercanciaController(
            RegistrarIngresoMercanciaUseCase registrarIngresoMercanciaUseCase) {

        this.registrarIngresoMercanciaUseCase =
                registrarIngresoMercanciaUseCase;
    }

    @PostMapping
    public void registrarIngreso(
            @AuthUserId UUID authUserId,
            @RequestBody RegistrarIngresoMercanciaRequest request) {

        registrarIngresoMercanciaUseCase.registrarIngreso(
                authUserId,
                request.getCentroDistribucionId(),
                request.getProductoId(),
                request.getCantidad(),
                request.getOrigen()
        );
    }
}