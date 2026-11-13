package com.example.optiway.infrastructure.adapter.in.rest;

import com.example.optiway.application.port.in.AjustarInventarioCDUseCase;
import com.example.optiway.application.port.in.AjustarInventarioUseCase;
import com.example.optiway.application.port.in.ObtenerInventarioCommand;
import com.example.optiway.application.port.in.ObtenerInventarioUseCase;
import com.example.optiway.application.port.in.dto.AjusteInventarioResponse;
import com.example.optiway.application.port.in.dto.InventarioResponse;
import com.example.optiway.infrastructure.adapter.in.rest.dto.AjustarInventarioCDRequest;
import com.example.optiway.infrastructure.adapter.in.rest.dto.AjustarInventarioRequest;
import com.example.optiway.infrastructure.security.AuthUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventario")
@Tag(name = "Inventario", description = "Endpoints para consulta y gestión de inventario en tiendas y centros de distribución")
public class InventarioController {

    private final ObtenerInventarioUseCase obtenerInventarioUseCase;
    private final AjustarInventarioCDUseCase ajustarInventarioCDUseCase;
    private final AjustarInventarioUseCase ajustarInventarioUseCase;

    public InventarioController(
            ObtenerInventarioUseCase obtenerInventarioUseCase,
            AjustarInventarioCDUseCase ajustarInventarioCDUseCase,
            AjustarInventarioUseCase ajustarInventarioUseCase) {
        this.obtenerInventarioUseCase = obtenerInventarioUseCase;
        this.ajustarInventarioCDUseCase = ajustarInventarioCDUseCase;
        this.ajustarInventarioUseCase = ajustarInventarioUseCase;
    }

    @GetMapping
    @Operation(summary = "Consultar inventario de tienda",
            description = "Obtiene los productos en inventario de una tienda específica.")
    public List<InventarioResponse> consultarInventario(
            @AuthUserId UUID authUserId,
            @Parameter(description = "ID numérico de la tienda", example = "1", required = true)
            @RequestParam Long tiendaId,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Long productoId) {

        return obtenerInventarioUseCase.obtenerInventario(
                authUserId,
                new ObtenerInventarioCommand(tiendaId, null, nombre, categoria, productoId));
    }

    @GetMapping("/centro-distribucion")
    @Operation(summary = "Consultar inventario de centro de distribución",
            description = "Obtiene los productos en inventario de un centro de distribución específico.")
    public List<InventarioResponse> consultarInventarioCD(
            @AuthUserId UUID authUserId,
            @Parameter(description = "ID numérico del centro de distribución", example = "1", required = true)
            @RequestParam Long centroDistribucionId,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Long productoId) {

        return obtenerInventarioUseCase.obtenerInventario(
                authUserId,
                new ObtenerInventarioCommand(null, centroDistribucionId, nombre, categoria, productoId));
    }

    @PatchMapping("/centro-distribucion/ajustar")
    @Operation(summary = "Ajustar inventario del centro de distribución", description = "Reemplaza la cantidad actual y registra fecha y encargado responsable. Solo el encargado asignado puede ajustar su CD.")
    public InventarioResponse ajustarInventarioCD(
            @AuthUserId UUID authUserId,
            @Valid @RequestBody AjustarInventarioCDRequest request) {
        return ajustarInventarioCDUseCase.ajustarInventario(
                authUserId, request.centroDistribucionId(), request.productoId(), request.nuevaCantidad());
    }

    @PostMapping("/ajustes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Ajustar inventario de tienda",
            description = "Registra una diferencia de inventario, actualiza las existencias y conserva el historial del ajuste.")
    public AjusteInventarioResponse ajustarInventario(
            @AuthUserId UUID authUserId,
            @Valid @RequestBody AjustarInventarioRequest request) {

        return ajustarInventarioUseCase.ajustarInventario(
                authUserId, request.toCommand());
    }
}
