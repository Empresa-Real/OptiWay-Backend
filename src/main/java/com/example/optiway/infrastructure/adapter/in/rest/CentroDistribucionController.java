package com.example.optiway.infrastructure.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import com.example.optiway.application.port.in.AsignarEncargadoCDUseCase;
import com.example.optiway.application.port.in.CrearCentroDistribucionUseCase;
import com.example.optiway.application.port.in.ListarCentrosDistribucionUseCase;
import com.example.optiway.application.port.in.RegistrarIngresoCommand;
import com.example.optiway.application.port.in.RegistrarIngresoMercanciaUseCase;
import com.example.optiway.application.port.in.dto.CentroDistribucionResponse;
import com.example.optiway.application.port.in.dto.IngresoMercanciaResponse;
import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.domain.model.IngresoMercancia;
import com.example.optiway.infrastructure.adapter.in.rest.dto.AsignarEncargadoRequest;
import com.example.optiway.infrastructure.adapter.in.rest.dto.CrearCentroDistribucionRequest;
import com.example.optiway.infrastructure.adapter.in.rest.dto.RegistrarIngresoRequest;
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
    private final ListarCentrosDistribucionUseCase listarCentrosDistribucionUseCase;
    private final AsignarEncargadoCDUseCase asignarEncargadoCDUseCase;
    private final RegistrarIngresoMercanciaUseCase registrarIngresoMercanciaUseCase;

    public CentroDistribucionController(
            CrearCentroDistribucionUseCase crearCentroDistribucionUseCase,
            ListarCentrosDistribucionUseCase listarCentrosDistribucionUseCase,
            AsignarEncargadoCDUseCase asignarEncargadoCDUseCase,
            RegistrarIngresoMercanciaUseCase registrarIngresoMercanciaUseCase) {
        this.crearCentroDistribucionUseCase = crearCentroDistribucionUseCase;
        this.listarCentrosDistribucionUseCase = listarCentrosDistribucionUseCase;
        this.asignarEncargadoCDUseCase = asignarEncargadoCDUseCase;
        this.registrarIngresoMercanciaUseCase = registrarIngresoMercanciaUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear centro de distribución", description = "Registra un nuevo centro de distribución con su capacidad y tiendas asignadas. Requiere rol ADMINISTRADOR.")
    public CentroDistribucionResponse crear(
            @Valid @RequestBody CrearCentroDistribucionRequest request,
            @AuthUserId UUID authUserId) {
        CentroDistribucion cd = crearCentroDistribucionUseCase.crearCentroDistribucion(request.toDomain(), authUserId);
        return CentroDistribucionResponse.from(cd);
    }

    @GetMapping
    @Operation(summary = "Listar centros de distribución", description = "Retorna los centros de distribución disponibles para el usuario según su rol.")
    public List<CentroDistribucionResponse> listar(@AuthUserId UUID authUserId) {
        return listarCentrosDistribucionUseCase.listarCentrosDistribucion(authUserId).stream()
                .map(CentroDistribucionResponse::from)
                .toList();
    }

    @PutMapping("/{id}/encargado")
    @Operation(summary = "Asignar encargado a centro de distribución", description = "Asigna o desasigna un usuario con rol ENCARGADO_CD como responsable. Requiere rol ADMINISTRADOR.")
    public CentroDistribucionResponse asignarEncargado(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) AsignarEncargadoRequest request,
            @AuthUserId UUID authUserId) {
        Long encargadoId = request != null ? request.encargadoId() : null;
        CentroDistribucion cd = asignarEncargadoCDUseCase.asignarEncargado(id, encargadoId, authUserId);
        return CentroDistribucionResponse.from(cd);
    }

    @PostMapping("/{id}/ingresos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar ingreso de mercancía", description = "Registra una entrada de stock de producto en el centro de distribución indicado.")
    public IngresoMercanciaResponse registrarIngreso(
            @PathVariable("id") Long centroDistribucionId,
            @Valid @RequestBody RegistrarIngresoRequest request,
            @AuthUserId UUID authUserId) {

        RegistrarIngresoCommand command = new RegistrarIngresoCommand(
                centroDistribucionId,
                request.getProductoId(),
                request.getCantidad(),
                request.getOrigen()
        );

        IngresoMercancia ingreso = registrarIngresoMercanciaUseCase.registrarIngreso(command, authUserId);
        return IngresoMercanciaResponse.from(ingreso);
    }
}

