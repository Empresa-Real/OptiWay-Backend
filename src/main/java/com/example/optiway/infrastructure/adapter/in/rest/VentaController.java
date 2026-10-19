package com.example.optiway.infrastructure.adapter.in.rest;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.optiway.application.port.in.RegistrarVentaCommand;
import com.example.optiway.application.port.in.RegistrarVentaUseCase;
import com.example.optiway.application.port.in.dto.VentaResponse;
import com.example.optiway.domain.model.Venta;
import com.example.optiway.infrastructure.adapter.in.rest.dto.RegistrarVentaRequest;
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
@RequestMapping("/api/ventas")
@Tag(name = "Ventas", description = "Endpoints para el registro de ventas en tiendas con descuento atómico de inventario")
public class VentaController {

    private final RegistrarVentaUseCase registrarVentaUseCase;

    public VentaController(RegistrarVentaUseCase registrarVentaUseCase) {
        this.registrarVentaUseCase = registrarVentaUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar venta", description = "Registra una transacción de venta en una tienda física, descontando inmediatamente las existencias del inventario local.")
    public VentaResponse registrarVenta(
            @Valid @RequestBody RegistrarVentaRequest request,
            @AuthUserId UUID authUserId) {
        RegistrarVentaCommand command = new RegistrarVentaCommand(
                request.tiendaId(),
                request.productoId(),
                request.cantidad(),
                request.fechaHora() != null ? request.fechaHora() : LocalDateTime.now()
        );

        Venta venta = registrarVentaUseCase.registrarVenta(command, authUserId);
        return VentaResponse.from(venta);
    }
}

