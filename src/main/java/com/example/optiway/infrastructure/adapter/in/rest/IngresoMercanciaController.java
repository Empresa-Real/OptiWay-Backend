package com.example.optiway.infrastructure.adapter.in.rest;

import java.util.UUID;

import com.example.optiway.application.port.in.RegistrarIngresoCommand;
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
    public void registrarIngreso(
            @RequestBody RegistrarIngresoMercanciaRequest request,
            @AuthUserId UUID authUserId) {

        RegistrarIngresoCommand command = new RegistrarIngresoCommand(
                request.getCentroDistribucionId(),
                request.getProductoId(),
                request.getCantidad(),
                request.getOrigen()
        );

        registrarIngresoMercanciaUseCase.registrarIngreso(command, authUserId);
    }
}