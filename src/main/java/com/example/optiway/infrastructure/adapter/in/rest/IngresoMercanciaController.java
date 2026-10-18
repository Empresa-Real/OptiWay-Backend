package com.example.optiway.infrastructure.adapter.in.rest;

import java.util.UUID;

import jakarta.validation.Valid;

import com.example.optiway.application.port.in.RegistrarIngresoCommand;
import com.example.optiway.domain.model.IngresoMercancia;
import com.example.optiway.infrastructure.adapter.in.rest.dto.IngresoMercanciaResponse;
import com.example.optiway.infrastructure.security.AuthUserId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.optiway.application.port.in.RegistrarIngresoMercanciaUseCase;

@RestController
@RequestMapping("/api/ingresos-mercancia")
public class IngresoMercanciaController {

    private final RegistrarIngresoMercanciaUseCase registrarIngresoMercanciaUseCase;

    public IngresoMercanciaController(
            RegistrarIngresoMercanciaUseCase registrarIngresoMercanciaUseCase) {

        this.registrarIngresoMercanciaUseCase = registrarIngresoMercanciaUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IngresoMercanciaResponse registrarIngreso(
            @Valid @RequestBody RegistrarIngresoMercanciaRequest request,
            @AuthUserId UUID authUserId) {

        RegistrarIngresoCommand command = new RegistrarIngresoCommand(
                request.getCentroDistribucionId(),
                request.getProductoId(),
                request.getCantidad(),
                request.getOrigen()
        );

        IngresoMercancia ingreso = registrarIngresoMercanciaUseCase.registrarIngreso(command, authUserId);
        return IngresoMercanciaResponse.from(ingreso);
    }
}