package com.example.optiway.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.optiway.application.port.in.AsignarEncargadoCDUseCase;
import com.example.optiway.application.port.in.CrearCentroDistribucionCommand;
import com.example.optiway.application.port.in.CrearCentroDistribucionUseCase;
import com.example.optiway.application.port.in.ObtenerCentrosDistribucionUseCase;
import com.example.optiway.application.port.in.dto.CentroDistribucionResponse;
import com.example.optiway.application.port.out.CentroDistribucionRepositoryPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.domain.model.Usuario;
import com.example.optiway.infrastructure.security.AuthorizationService;

@Service
public class CentroDistribucionService implements CrearCentroDistribucionUseCase, ObtenerCentrosDistribucionUseCase, AsignarEncargadoCDUseCase {

    private final CentroDistribucionRepositoryPort centroDistribucionRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final AuthorizationService authorizationService;

    public CentroDistribucionService(
            CentroDistribucionRepositoryPort centroDistribucionRepositoryPort,
            UsuarioRepositoryPort usuarioRepositoryPort,
            AuthorizationService authorizationService) {
        this.centroDistribucionRepositoryPort = centroDistribucionRepositoryPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.authorizationService = authorizationService;
    }

    @Override
    public CentroDistribucionResponse crearCentroDistribucion(UUID authUserId, CrearCentroDistribucionCommand command) {
        authorizationService.requireAdministrador(authUserId, "crear Centros de Distribución");

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
        if (authUserId == null) {
            return mapearRespuestas(centroDistribucionRepositoryPort.obtenerTodos());
        }

        Usuario usuario = authorizationService.requireAuthenticated(authUserId);

        if (usuario.puedeVerTodoElCatalogoLogistico()) {
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
        authorizationService.requireAdministrador(authUserId, "asignar encargados a Centros de Distribución");

        CentroDistribucion centro = centroDistribucionRepositoryPort.obtenerPorId(centroId)
                .orElseThrow(() -> new IllegalArgumentException("El centro de distribución no existe."));

        if (encargadoId != null) {
            Usuario encargado = usuarioRepositoryPort.obtenerPorId(encargadoId)
                    .orElseThrow(() -> new IllegalArgumentException("El usuario encargado no existe."));

            if (!encargado.esEncargadoCD()) {
                throw new IllegalArgumentException(
                        "Solo se puede asignar un centro de distribución a un usuario con rol Encargado de CD (Rol actual: "
                                + encargado.getRol() + ").");
            }
        }

        centro.setEncargadoId(encargadoId);
        return CentroDistribucionResponse.from(centroDistribucionRepositoryPort.guardar(centro));
    }
}
