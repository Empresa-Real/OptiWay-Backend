package com.example.optiway.infrastructure.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import com.example.optiway.application.port.in.AsignarEncargadoTiendaUseCase;
import com.example.optiway.application.port.in.CambiarEstadoTiendaUseCase;
import com.example.optiway.application.port.in.CrearTiendaUseCase;
import com.example.optiway.application.port.in.ObtenerTiendasUseCase;
import com.example.optiway.application.port.in.RegistrarVentaUseCase;
import com.example.optiway.application.port.in.dto.OrdenesEnTransitoResponse;
import com.example.optiway.application.port.in.dto.TiendaResponse;
import com.example.optiway.application.port.in.dto.VentaResponse;
import com.example.optiway.infrastructure.adapter.in.rest.dto.AsignarEncargadoRequest;
import com.example.optiway.infrastructure.adapter.in.rest.dto.CambiarEstadoTiendaRequest;
import com.example.optiway.infrastructure.adapter.in.rest.dto.CrearTiendaRequest;
import com.example.optiway.infrastructure.adapter.in.rest.dto.RegistrarVentaRequest;
import com.example.optiway.infrastructure.security.AuthUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tiendas")
@Tag(name = "Tiendas", description = "Endpoints para registro, consulta, asignación de encargados, cambio de estado y ventas en tiendas")
public class TiendaController {

    private final CrearTiendaUseCase crearTiendaUseCase;
    private final ObtenerTiendasUseCase obtenerTiendasUseCase;
    private final AsignarEncargadoTiendaUseCase asignarEncargadoTiendaUseCase;
    private final RegistrarVentaUseCase registrarVentaUseCase;
    private final CambiarEstadoTiendaUseCase cambiarEstadoTiendaUseCase;

    public TiendaController(
            CrearTiendaUseCase crearTiendaUseCase,
            ObtenerTiendasUseCase obtenerTiendasUseCase,
            AsignarEncargadoTiendaUseCase asignarEncargadoTiendaUseCase,
            RegistrarVentaUseCase registrarVentaUseCase,
            CambiarEstadoTiendaUseCase cambiarEstadoTiendaUseCase) {
        this.crearTiendaUseCase = crearTiendaUseCase;
        this.obtenerTiendasUseCase = obtenerTiendasUseCase;
        this.asignarEncargadoTiendaUseCase = asignarEncargadoTiendaUseCase;
        this.registrarVentaUseCase = registrarVentaUseCase;
        this.cambiarEstadoTiendaUseCase = cambiarEstadoTiendaUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear nueva tienda", description = "Registra una tienda en el sistema generando su código único. Requiere rol ADMINISTRADOR.")
    public TiendaResponse crearTienda(
            @AuthUserId UUID authUserId,
            @Valid @RequestBody CrearTiendaRequest request) {
        return crearTiendaUseCase.crearTienda(authUserId, request.toCommand());
    }

    @GetMapping
    @Operation(summary = "Listar tiendas", description = "Retorna el listado de tiendas registradas en el sistema.")
    public List<TiendaResponse> obtenerTiendas(@AuthUserId UUID authUserId) {
        return obtenerTiendasUseCase.obtenerTiendas(authUserId);
    }

    @PutMapping("/{id}/encargado")
    @Operation(summary = "Asignar encargado a tienda", description = "Asigna o desasigna un usuario con rol TIENDA como encargado. Requiere rol ADMINISTRADOR.")
    public TiendaResponse asignarEncargado(
            @AuthUserId UUID authUserId,
            @Parameter(description = "ID numérico (Long) de la tienda", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody(required = false) AsignarEncargadoRequest request) {
        Long encargadoId = request != null ? request.encargadoId() : null;
        return asignarEncargadoTiendaUseCase.asignarEncargado(authUserId, id, encargadoId);
    }

    @PostMapping("/{id}/ventas")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar venta", description = "Registra una transacción de venta en la tienda indicada en la URL, descontando inmediatamente las existencias del inventario local.")
    public VentaResponse registrarVenta(
            @AuthUserId UUID authUserId,
            @Parameter(description = "ID numérico (Long) de la tienda", example = "1")
            @PathVariable("id") Long tiendaId,
            @Valid @RequestBody RegistrarVentaRequest request) {
        return registrarVentaUseCase.registrarVenta(authUserId, request.toCommand(tiendaId));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Desactivar o activar tienda", description = "Cambia el estado activo/inactivo de la tienda validando órdenes de reposición en tránsito. Requiere rol ADMINISTRADOR.")
    public TiendaResponse cambiarEstadoTienda(
            @AuthUserId UUID authUserId,
            @Parameter(description = "ID numérico (Long) de la tienda", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoTiendaRequest request) {
        return cambiarEstadoTiendaUseCase.cambiarEstado(authUserId, id, request);
    }

    @GetMapping("/{id}/ordenes-en-transito")
    @Operation(summary = "Verificar órdenes en tránsito", description = "Retorna si la tienda tiene órdenes de reposición en tránsito o enviadas para gestionar modales de confirmación en el frontend.")
    public OrdenesEnTransitoResponse verificarOrdenesEnTransito(
            @AuthUserId UUID authUserId,
            @Parameter(description = "ID numérico (Long) de la tienda", example = "1")
            @PathVariable Long id) {
        boolean tieneOrdenes = cambiarEstadoTiendaUseCase.tieneOrdenesEnTransitoOEnviadas(id);
        return new OrdenesEnTransitoResponse(tieneOrdenes);
    }
}