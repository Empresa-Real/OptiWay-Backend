package com.example.optiway.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.optiway.application.port.in.SolicitarReposicionUseCase;
import com.example.optiway.application.port.in.dto.SolicitudReposicionResponse;
import com.example.optiway.application.port.out.AuthorizationPort;
import com.example.optiway.infrastructure.adapter.in.rest.dto.CrearSolicitudReposicionRequest;

@Service
public class ReposicionService implements SolicitarReposicionUseCase {

    private final AuthorizationPort authorizationService;
    // Inyecta aquí tus repositorios necesarios (ej. SolicitudReposicionRepositoryPort, ProductoRepositoryPort, etc.)

    public ReposicionService(AuthorizationPort authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Override
    public SolicitudReposicionResponse crearSolicitud(UUID authUserId, Long tiendaId, CrearSolicitudReposicionRequest request) {
        // Validar rol ENCARGADO_TIENDA o permisos equivalentes
        // authorizationService.requireEncargadoTienda(authUserId, ...);

        if (request.getCantidad() == null || request.getCantidad() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad debe ser mayor o igual a 1");
        }

        // TODO:Guardar en base de datos mediante repositorio y retornar respuesta real
        return new SolicitudReposicionResponse(
            1L, 
            tiendaId, 
            request.getProductoId(), 
            "Producto Ejemplo", 
            request.getCantidad(), 
            "PENDIENTE", 
            authUserId, 
            LocalDateTime.now()
        );
    }

    @Override
    public List<SolicitudReposicionResponse> obtenerTodasLasSolicitudes(UUID authUserId) {
        // Validar rol PLANIFICADOR
        // authorizationService.requirePlanificador(authUserId, ...);

        // TODO: Retornar listado desde base de datos para el Planificador
        return List.of();
    }
}