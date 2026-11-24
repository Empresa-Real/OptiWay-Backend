package com.example.optiway.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.optiway.application.exception.RecursoNoEncontradoException;
import com.example.optiway.application.port.in.AsignarEncargadoCDUseCase;
import com.example.optiway.application.port.in.CambiarEstadoCentroDistribucionUseCase;
import com.example.optiway.application.port.in.CrearCentroDistribucionCommand;
import com.example.optiway.application.port.in.CrearCentroDistribucionUseCase;
import com.example.optiway.application.port.in.ObtenerCentrosDistribucionUseCase;
import com.example.optiway.application.port.in.ObtenerOrdenesCDUseCase;
import com.example.optiway.application.port.in.dto.CentroDistribucionResponse;
import com.example.optiway.application.port.in.dto.OrdenReposicionResponse;
import com.example.optiway.application.port.out.CentroDistribucionRepositoryPort;
import com.example.optiway.application.port.out.OrdenReposicionRepositoryPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.domain.model.Usuario;
import com.example.optiway.application.port.out.AuthorizationPort;
import com.example.optiway.infrastructure.security.AuthorizedAction;

@Service
public class CentroDistribucionService implements CrearCentroDistribucionUseCase, ObtenerCentrosDistribucionUseCase, AsignarEncargadoCDUseCase, CambiarEstadoCentroDistribucionUseCase, ObtenerOrdenesCDUseCase {

    private final CentroDistribucionRepositoryPort centroDistribucionRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final AuthorizationPort authorizationService;
    private final OrdenReposicionRepositoryPort ordenReposicionRepositoryPort;

    public CentroDistribucionService(
            CentroDistribucionRepositoryPort centroDistribucionRepositoryPort,
            UsuarioRepositoryPort usuarioRepositoryPort,
            AuthorizationPort authorizationService) {
        this(centroDistribucionRepositoryPort, usuarioRepositoryPort, authorizationService, null);
    }

    @Autowired
    public CentroDistribucionService(
            CentroDistribucionRepositoryPort centroDistribucionRepositoryPort,
            UsuarioRepositoryPort usuarioRepositoryPort,
            AuthorizationPort authorizationService,
            OrdenReposicionRepositoryPort ordenReposicionRepositoryPort) {
        this.centroDistribucionRepositoryPort = centroDistribucionRepositoryPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.authorizationService = authorizationService;
        this.ordenReposicionRepositoryPort = ordenReposicionRepositoryPort;
    }

    @Override
    public CentroDistribucionResponse crearCentroDistribucion(UUID authUserId, CrearCentroDistribucionCommand command) {
        authorizationService.requireGestionLogistica(authUserId, AuthorizedAction.CREAR_CENTRO_DISTRIBUCION);

        String nombre = command.nombre().trim();
        String direccion = command.direccion().trim();

        CentroDistribucion centro = new CentroDistribucion();
        centro.setNombre(nombre);
        centro.setDireccion(direccion);
        centro.setCapacidad(command.capacidad());
        centro.setEncargadoId(command.encargadoId());
        centrosAbastecidos(command, centro);

        String codigoUnico = "CD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        centro.setCodigo(codigoUnico);

        return CentroDistribucionResponse.from(centroDistribucionRepositoryPort.guardar(centro));
    }

    private void centrosAbastecidos(CrearCentroDistribucionCommand command, CentroDistribucion centro) {
        if (command.tiendasAbastecidasIds() != null) {
            centro.setTiendasAbastecidasIds(command.tiendasAbastecidasIds());
        }
    }

    @Override
    public List<CentroDistribucionResponse> obtenerCentrosDistribucion(UUID authUserId) {
        Usuario usuario = authorizationService.requireAuthenticated(authUserId);

        if (usuario.puedeGestionarTodo()) {
            return mapearRespuestas(centroDistribucionRepositoryPort.obtenerTodos());
        }

        if (usuario.esEncargadoCD()) {
            return mapearRespuestas(centroDistribucionRepositoryPort.obtenerPorEncargadoId(usuario.getId()));
        }

        return List.of();
    }

    private List<CentroDistribucionResponse> mapearRespuestas(List<CentroDistribucion> centros) {
        return centros.stream()
                .map(CentroDistribucionResponse::from)
                .toList();
    }

    @Override
    public CentroDistribucionResponse asignarEncargado(UUID authUserId, Long centroId, Long encargadoId) {
        authorizationService.requireGestionLogistica(authUserId, AuthorizedAction.ASIGNAR_ENCARGADO_CENTRO_DISTRIBUCION);

        CentroDistribucion centro = centroDistribucionRepositoryPort.obtenerPorId(centroId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Centro de distribución no encontrado con ID: " + centroId));

        if (encargadoId != null) {
            Usuario encargado = usuarioRepositoryPort.obtenerPorId(encargadoId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Usuario encargado no encontrado con ID: " + encargadoId));

            if (!encargado.admiteAsignacionDeUbicacion()) {
                throw new IllegalArgumentException(
                        "No se puede asignar una ubicación a un usuario Administrador o Planificador (Rol actual: "
                                + encargado.getRol() + ").");
            }

            if (!encargado.esEncargadoCD()) {
                throw new IllegalArgumentException(
                        "Solo se puede asignar un centro de distribución a un usuario con rol Encargado de CD (Rol actual: "
                                + encargado.getRol() + ").");
            }
        }

        centro.setEncargadoId(encargadoId);
        return CentroDistribucionResponse.from(centroDistribucionRepositoryPort.guardar(centro));
    }

    @Override
    public CentroDistribucionResponse cambiarEstado(UUID authUserId, Long centroId, boolean activo, boolean confirmarConTiendasAsignadas) {
        authorizationService.requireAdministrador(authUserId, AuthorizedAction.CAMBIAR_ESTADO_CENTRO_DISTRIBUCION);
        CentroDistribucion centro = centroDistribucionRepositoryPort.obtenerPorId(centroId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Centro de distribución no encontrado con ID: " + centroId));
        if (!activo && centro.getTiendasAbastecidasIds() != null
                && !centro.getTiendasAbastecidasIds().isEmpty() && !confirmarConTiendasAsignadas) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "El centro tiene tiendas asignadas. Confirme la desactivación con confirmarConTiendasAsignadas=true.");
        }
        centro.setEstado(activo ? "Activo" : "Inactivo");
        return CentroDistribucionResponse.from(centroDistribucionRepositoryPort.guardar(centro));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenReposicionResponse> obtenerOrdenesPorCentro(UUID authUserId, Long centroDistribucionId) {
        if (centroDistribucionId == null || centroDistribucionId <= 0) {
            throw new IllegalArgumentException("El ID del centro de distribución debe ser positivo.");
        }
        CentroDistribucion centro = centroDistribucionRepositoryPort.obtenerPorId(centroDistribucionId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Centro de distribución no encontrado con ID: " + centroDistribucionId));

        authorizationService.requirePermisoCentroDistribucion(
                authUserId, centro, AuthorizedAction.LISTAR_ORDENES_CENTRO_DISTRIBUCION);

        if (ordenReposicionRepositoryPort == null) {
            return List.of();
        }

        return ordenReposicionRepositoryPort.listarPorCentroDistribucionOrigen(centroDistribucionId).stream()
                .map(OrdenReposicionResponse::from)
                .toList();
    }
}
