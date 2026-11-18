package com.example.optiway.infrastructure.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.optiway.application.port.in.AprobarRecomendacionCommand;
import com.example.optiway.application.port.in.AprobarRecomendacionUseCase;
import com.example.optiway.application.port.in.ConsultarRecomendacionesReposicionUseCase;
import com.example.optiway.application.port.in.CrearRecomendacionReposicionUseCase;
import com.example.optiway.application.port.in.RechazarRecomendacionUseCase;
import com.example.optiway.application.port.in.dto.OrdenReposicionResponse;
import com.example.optiway.application.port.in.dto.RecomendacionReposicionResponse;
import com.example.optiway.infrastructure.adapter.in.rest.dto.AprobarRecomendacionRequest;
import com.example.optiway.infrastructure.adapter.in.rest.dto.CrearRecomendacionReposicionRequest;
import com.example.optiway.infrastructure.adapter.in.rest.dto.RechazarRecomendacionRequest;
import com.example.optiway.infrastructure.security.AuthUserId;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/recomendaciones-reposicion")
@Tag(
        name = "Recomendaciones de reposición",
        description = "Creación, consulta y resolución (aprobar, ajustar o rechazar) de propuestas de reposición"
)
public class RecomendacionReposicionController {

    private final CrearRecomendacionReposicionUseCase crearUseCase;
    private final ConsultarRecomendacionesReposicionUseCase consultarUseCase;
    private final AprobarRecomendacionUseCase aprobarUseCase;
    private final RechazarRecomendacionUseCase rechazarUseCase;

    public RecomendacionReposicionController(
            CrearRecomendacionReposicionUseCase crearUseCase,
            ConsultarRecomendacionesReposicionUseCase consultarUseCase,
            AprobarRecomendacionUseCase aprobarUseCase,
            RechazarRecomendacionUseCase rechazarUseCase) {
        this.crearUseCase = crearUseCase;
        this.consultarUseCase = consultarUseCase;
        this.aprobarUseCase = aprobarUseCase;
        this.rechazarUseCase = rechazarUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Crear recomendación de reposición",
            description = "Registra una propuesta de traslado de inventario. Requiere rol PLANIFICADOR."
    )
    public RecomendacionReposicionResponse crear(
            @AuthUserId UUID authUserId,
            @Valid @RequestBody CrearRecomendacionReposicionRequest request) {

        return crearUseCase.crear(authUserId, request.toCommand());
    }

    @GetMapping
    @Operation(
            summary = "Consultar recomendaciones de reposición",
            description = "Lista las propuestas registradas. Requiere rol PLANIFICADOR."
    )
    public List<RecomendacionReposicionResponse> consultar(
            @AuthUserId UUID authUserId) {

        return consultarUseCase.consultar(authUserId);
    }

    @PatchMapping("/{id}/aprobar")
    @Operation(
            summary = "Aprobar o ajustar y aprobar recomendación",
            description = "Aprueba una recomendación en estado PROPUESTA (el cuerpo es opcional; si se envía, permite ajustar cantidad o CD origen) y retorna 200 OK con la Orden de Reposición generada en estado APROBADA. Requiere rol PLANIFICADOR."
    )
    public OrdenReposicionResponse aprobar(
            @AuthUserId UUID authUserId,
            @Parameter(description = "ID numérico de la recomendación", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody(required = false) AprobarRecomendacionRequest request) {

        AprobarRecomendacionCommand command = request != null
                ? request.toCommand(id)
                : new AprobarRecomendacionCommand(id, null, null, null);
        return aprobarUseCase.aprobar(authUserId, command);
    }

    @PatchMapping("/{id}/rechazar")
    @Operation(
            summary = "Rechazar recomendación de reposición",
            description = "Marca una recomendación en estado PROPUESTA como RECHAZADA registrando el motivo obligatorio. Requiere rol PLANIFICADOR."
    )
    public RecomendacionReposicionResponse rechazar(
            @AuthUserId UUID authUserId,
            @Parameter(description = "ID numérico de la recomendación", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody RechazarRecomendacionRequest request) {

        return rechazarUseCase.rechazar(authUserId, request.toCommand(id));
    }
}
