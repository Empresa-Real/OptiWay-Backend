package com.example.optiway.infrastructure.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.optiway.application.port.in.ConsultarRecomendacionesReposicionUseCase;
import com.example.optiway.application.port.in.CrearRecomendacionReposicionUseCase;
import com.example.optiway.application.port.in.dto.RecomendacionReposicionResponse;
import com.example.optiway.infrastructure.adapter.in.rest.dto.CrearRecomendacionReposicionRequest;
import com.example.optiway.infrastructure.security.AuthUserId;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/recomendaciones-reposicion")
@Tag(
        name = "Recomendaciones de reposición",
        description = "Creación y consulta de propuestas de reposición de inventario"
)
public class RecomendacionReposicionController {

    private final CrearRecomendacionReposicionUseCase crearUseCase;
    private final ConsultarRecomendacionesReposicionUseCase consultarUseCase;

    public RecomendacionReposicionController(
            CrearRecomendacionReposicionUseCase crearUseCase,
            ConsultarRecomendacionesReposicionUseCase consultarUseCase) {
        this.crearUseCase = crearUseCase;
        this.consultarUseCase = consultarUseCase;
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
}