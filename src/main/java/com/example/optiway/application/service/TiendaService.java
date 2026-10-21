package com.example.optiway.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.optiway.application.exception.RecursoNoEncontradoException;
import com.example.optiway.application.port.in.AsignarEncargadoTiendaUseCase;
import com.example.optiway.application.port.in.CrearTiendaCommand;
import com.example.optiway.application.port.in.CrearTiendaUseCase;
import com.example.optiway.application.port.in.ObtenerTiendasUseCase;
import com.example.optiway.application.port.in.dto.TiendaResponse;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.domain.model.Tienda;
import com.example.optiway.domain.model.Usuario;
import com.example.optiway.application.port.out.AuthorizationPort;
import com.example.optiway.infrastructure.security.AccionAutorizada;

@Service
public class TiendaService implements CrearTiendaUseCase, ObtenerTiendasUseCase, AsignarEncargadoTiendaUseCase {

    private final TiendaRepositoryPort tiendaRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final AuthorizationPort authorizationService;

    public TiendaService(
            TiendaRepositoryPort tiendaRepositoryPort,
            UsuarioRepositoryPort usuarioRepositoryPort,
            AuthorizationPort authorizationService) {
        this.tiendaRepositoryPort = tiendaRepositoryPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.authorizationService = authorizationService;
    }

    @Override
    public TiendaResponse crearTienda(UUID authUserId, CrearTiendaCommand command) {
        authorizationService.requireGestionLogistica(authUserId, AccionAutorizada.CREAR_TIENDA);

        String nombre = command.nombre().trim();
        String direccion = command.direccion().trim();
        String ciudad = command.ciudad().trim();

        Tienda tienda = new Tienda();
        tienda.setNombre(nombre);
        tienda.setDireccion(direccion);
        tienda.setCiudad(ciudad);
        tienda.setEncargadoId(command.encargadoId());
        tienda.setCodigo("TND-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        tienda.setActivo(true);

        return TiendaResponse.from(tiendaRepositoryPort.save(tienda));
    }

    @Override
    public List<TiendaResponse> obtenerTiendas(UUID authUserId) {
        Usuario usuario = authorizationService.requireAuthenticated(authUserId);

        if (usuario.puedeGestionarTodo()) {
            return mapearRespuestas(tiendaRepositoryPort.findAll());
        }

        if (usuario.esEncargadoTienda()) {
            return mapearRespuestas(tiendaRepositoryPort.obtenerPorEncargadoId(usuario.getId()));
        }

        return List.of();
    }

    private List<TiendaResponse> mapearRespuestas(List<Tienda> tiendas) {
        return tiendas.stream()
                .map(TiendaResponse::from)
                .toList();
    }

    @Override
    public TiendaResponse asignarEncargado(UUID authUserId, Long tiendaId, Long encargadoId) {
        authorizationService.requireGestionLogistica(authUserId, AccionAutorizada.ASIGNAR_ENCARGADO_TIENDA);

        Tienda tienda = tiendaRepositoryPort.obtenerPorId(tiendaId)
                .orElseThrow(() -> new IllegalArgumentException("La tienda no existe."));

        if (encargadoId != null) {
            Usuario encargado = usuarioRepositoryPort.obtenerPorId(encargadoId)
                    .orElseThrow(() -> new IllegalArgumentException("El usuario encargado no existe."));

            if (!encargado.admiteAsignacionDeUbicacion()) {
                throw new IllegalArgumentException(
                        "No se puede asignar una ubicación a un usuario Administrador o Planificador (Rol actual: "
                                + encargado.getRol() + ").");
            }

            if (!encargado.esEncargadoTienda()) {
                throw new IllegalArgumentException(
                        "Solo se puede asignar una tienda a un usuario con rol Encargado de tienda (Rol actual: "
                                + encargado.getRol() + ").");
            }
        }

        tienda.setEncargadoId(encargadoId);
        return TiendaResponse.from(tiendaRepositoryPort.save(tienda));
    }
}
