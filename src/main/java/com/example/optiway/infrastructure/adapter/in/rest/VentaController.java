package com.example.optiway.infrastructure.adapter.in.rest;

import com.example.optiway.application.port.in.RegistrarVentaCommand;
import com.example.optiway.application.port.in.RegistrarVentaUseCase;
import com.example.optiway.domain.model.Venta;
import com.example.optiway.infrastructure.adapter.in.rest.dto.RegistrarVentaRequest;
import com.example.optiway.infrastructure.security.AuthUserId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final RegistrarVentaUseCase registrarVentaUseCase;

    public VentaController(RegistrarVentaUseCase registrarVentaUseCase) {
        this.registrarVentaUseCase = registrarVentaUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Venta registrarVenta(
            @RequestBody RegistrarVentaRequest request,
            @AuthUserId UUID authUserId) {
        RegistrarVentaCommand command = new RegistrarVentaCommand(
                request.getTiendaId(),
                request.getProductoId(),
                request.getCantidad(),
                request.getFechaHora() != null ? request.getFechaHora() : LocalDateTime.now()
        );

        return registrarVentaUseCase.registrarVenta(command, authUserId);
    }
}
