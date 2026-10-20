package com.example.optiway.infrastructure.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import com.example.optiway.application.port.in.AsignarEncargadoCDUseCase;
import com.example.optiway.application.port.in.CrearCentroDistribucionUseCase;
import com.example.optiway.application.port.in.ObtenerCentrosDistribucionUseCase;
import com.example.optiway.application.port.in.RegistrarIngresoMercanciaUseCase;
import com.example.optiway.application.port.in.dto.CentroDistribucionResponse;
import com.example.optiway.application.port.in.dto.IngresoMercanciaResponse;
import com.example.optiway.infrastructure.adapter.in.rest.dto.AsignarEncargadoRequest;
import com.example.optiway.infrastructure.adapter.in.rest.dto.CrearCentroDistribucionRequest;
import com.example.optiway.infrastructure.adapter.in.rest.dto.RegistrarIngresoMercanciaRequest;
import com.example.optiway.infrastructure.security.AuthUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/centros-distribucion", "/api/cd"})
@Tag(name = "Centros de Distribución", description = "Endpoints para registro, consulta, asignación de encargados e ingreso de mercancía en centros de distribución")
public class CentroDistribucionController {

    private final CrearCentroDistribucionUseCase crearCentroDistribucionUseCase;
    private final ObtenerCentrosDistribucionUseCase obtenerCentrosDistribucionUseCase;
    private final AsignarEncargadoCDUseCase asignarEncargadoCDUseCase;
    private final RegistrarIngresoMercanciaUseCase registrarIngresoMercanciaUseCase;

    public CentroDistribucionController(
            CrearCentroDistribucionUseCase crearCentroDistribucionUseCase,
            ObtenerCentrosDistribucionUseCase obtenerCentrosDistribucionUseCase,
            AsignarEncargadoCDUseCase asignarEncargadoCDUseCase,
            RegistrarIngresoMercanciaUseCase registrarIngresoMercanciaUseCase) {
        this.crearCentroDistribucionUseCase = crearCentroDistribucionUseCase;
        this.obtenerCentrosDistribucionUseCase = obtenerCentrosDistribucionUseCase;
        this.asignarEncargadoCDUseCase = asignarEncargadoCDUseCase;
        this.registrarIngresoMercanciaUseCase = registrarIngresoMercanciaUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear centro de distribución", description = "Registra un nuevo centro de distribución con su capacidad y tiendas asignadas. Requiere rol ADMINISTRADOR.")
    public CentroDistribucionResponse crear(
            @AuthUserId UUID authUserId,
            @Valid @RequestBody CrearCentroDistribucionRequest request) {
        return crearCentroDistribucionUseCase.crearCentroDistribucion(authUserId, request.toCommand());
    }

    @GetMapping
    @Operation(summary = "Listar centros de distribución", description = "Retorna los centros de distribución disponibles para el usuario según su rol.")
    public List<CentroDistribucionResponse> listar(@AuthUserId UUID authUserId) {
        return obtenerCentrosDistribucionUseCase.obtenerCentrosDistribucion(authUserId);
    }

    @PutMapping("/{id}/encargado")
    @Operation(summary = "Asignar encargado a centro de distribución", description = "Asigna o desasigna un usuario con rol ENCARGADO_CD como responsable. Requiere rol ADMINISTRADOR.")
    public CentroDistribucionResponse asignarEncargado(
            @AuthUserId UUID authUserId,
            @PathVariable Long id,
            @Valid @RequestBody(required = false) AsignarEncargadoRequest request) {
        Long encargadoId = request != null ? request.encargadoId() : null;
        return asignarEncargadoCDUseCase.asignarEncargado(authUserId, id, encargadoId);
    }

    @PostMapping("/{id}/ingresos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar ingreso de mercancía", description = "Registra una entrada de stock en el centro de distribución indicado en la URL. Alias corto: POST /api/cd/{id}/ingresos.")
    public IngresoMercanciaResponse registrarIngreso(
            @AuthUserId UUID authUserId,
            @PathVariable("id") Long centroDistribucionId,
            @Valid @RequestBody RegistrarIngresoMercanciaRequest request) {
        return registrarIngresoMercanciaUseCase.registrarIngreso(authUserId, request.toCommand(centroDistribucionId));
    }
}
