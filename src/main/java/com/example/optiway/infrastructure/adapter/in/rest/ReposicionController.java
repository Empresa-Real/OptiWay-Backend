package com.example.optiway.infrastructure.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.optiway.application.port.in.SolicitarReposicionUseCase;
import com.example.optiway.application.port.in.dto.SolicitudReposicionResponse;
import com.example.optiway.infrastructure.adapter.in.rest.dto.CrearSolicitudReposicionRequest;
import com.example.optiway.infrastructure.security.AuthUserId;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@Tag(name = "Reposiciones", description = "Endpoints para la gestión y solicitud manual de reposiciones")
public class ReposicionController {

    private final SolicitarReposicionUseCase solicitarReposicionUseCase;

    public ReposicionController(SolicitarReposicionUseCase solicitarReposicionUseCase) {
        this.solicitarReposicionUseCase = solicitarReposicionUseCase;
    }

    @PostMapping("/tiendas/{id}/solicitudes-reposicion")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Solicitar reposición manual", description = "Permite al encargado de tienda solicitar reposición de productos. Rol: ENCARGADO_TIENDA.")
    public SolicitudReposicionResponse crearSolicitudReposicion(
            @AuthUserId UUID authUserId,
            @Parameter(description = "ID de la tienda", example = "1")
            @PathVariable("id") Long tiendaId,
            @Valid @RequestBody CrearSolicitudReposicionRequest request) {
        return solicitarReposicionUseCase.crearSolicitud(authUserId, tiendaId, request);
    }

    @GetMapping("/solicitudes-reposicion")
    @Operation(summary = "Listar solicitudes de reposición", description = "Permite al planificador visualizar todas las solicitudes de reposición pendientes. Rol: PLANIFICADOR.")
    public List<SolicitudReposicionResponse> obtenerSolicitudesReposicion(@AuthUserId UUID authUserId) {
        return solicitarReposicionUseCase.obtenerTodasLasSolicitudes(authUserId);
    }
}