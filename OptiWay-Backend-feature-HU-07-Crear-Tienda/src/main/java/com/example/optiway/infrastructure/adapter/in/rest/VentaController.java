package com.example.optiway.infrastructure.adapter.in.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.optiway.application.port.in.RegistrarVentaCommand;
import com.example.optiway.application.port.in.RegistrarVentaUseCase;
import com.example.optiway.domain.model.Venta;
import com.example.optiway.infrastructure.adapter.in.rest.dto.RegistrarVentaRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final RegistrarVentaUseCase registrarVentaUseCase;

    public VentaController(RegistrarVentaUseCase registrarVentaUseCase) {
        this.registrarVentaUseCase = registrarVentaUseCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('ENCARGADO_TIENDA')")
    public ResponseEntity<Venta> registrarVenta(@Valid @RequestBody RegistrarVentaRequest request) {
        RegistrarVentaCommand command = new RegistrarVentaCommand(
                request.getTiendaId(),
                request.getProductoId(),
                request.getCantidad(),
                request.getFechaHora() != null ? request.getFechaHora() : java.time.LocalDateTime.now()
        );

        Venta ventaRegistrada = registrarVentaUseCase.registrarVenta(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ventaRegistrada);
    }
}