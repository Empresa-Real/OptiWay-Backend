package com.example.optiway.application.service;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.optiway.application.exception.AccesoDenegadoException;
import com.example.optiway.application.exception.UsuarioDuplicadoException;
import com.example.optiway.application.port.in.ActualizarRolCommand;
import com.example.optiway.application.port.in.ActualizarRolUsuarioUseCase;
import com.example.optiway.application.port.in.CrearUsuarioCommand;
import com.example.optiway.application.port.in.CrearUsuarioUseCase;
import com.example.optiway.application.port.in.EliminarUsuarioUseCase;
import com.example.optiway.application.port.in.ObtenerUsuarioUseCase;
import com.example.optiway.application.port.in.ObtenerUsuariosUseCase;
import com.example.optiway.application.port.in.dto.UsuarioResponse;
import com.example.optiway.application.port.in.dto.AsignacionUbicacion;
import com.example.optiway.application.port.out.AuthPort;
import com.example.optiway.application.port.out.CentroDistribucionRepositoryPort;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.Usuario;
import com.example.optiway.infrastructure.adapter.out.auth.SupabaseAuthException;
import com.example.optiway.application.port.out.AuthorizationPort;
import com.example.optiway.infrastructure.security.AuthorizedAction;

@Service
public class UsuarioService implements CrearUsuarioUseCase, ObtenerUsuariosUseCase,
    ObtenerUsuarioUseCase, EliminarUsuarioUseCase, ActualizarRolUsuarioUseCase {
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final TiendaRepositoryPort tiendaRepositoryPort;
    private final CentroDistribucionRepositoryPort centroDistribucionRepositoryPort;
    private final AuthPort authPort;
    private final AuthorizationPort authorizationService;
    private final AuthErrorMapper authErrorMapper;

    public UsuarioService(
            UsuarioRepositoryPort usuarioRepositoryPort,
            TiendaRepositoryPort tiendaRepositoryPort,
            CentroDistribucionRepositoryPort centroDistribucionRepositoryPort,
            AuthPort authPort,
            AuthorizationPort authorizationService,
            AuthErrorMapper authErrorMapper) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.tiendaRepositoryPort = tiendaRepositoryPort;
        this.centroDistribucionRepositoryPort = centroDistribucionRepositoryPort;
        this.authPort = authPort;
        this.authorizationService = authorizationService;
        this.authErrorMapper = authErrorMapper;
    }

    @Override
    public UsuarioResponse crearUsuario(UUID authUserId, CrearUsuarioCommand command) {
        authorizationService.requireAdministrador(authUserId, AuthorizedAction.CREAR_USUARIO);

        String nombre = command.nombre().trim();
        String email = command.email().trim().toLowerCase();
        Rol rol = command.rol();

        if (usuarioRepositoryPort.existePorEmail(email)) {
            throw new UsuarioDuplicadoException("El correo ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setRol(rol);
        usuario.setCreadoPor(authUserId);
        usuario.setAuthUserId(invitarEnAuth(email));
        return UsuarioResponse.from(usuarioRepositoryPort.guardar(usuario), null);
    }

    @Override
    public List<UsuarioResponse> obtenerUsuarios(UUID authUserId) {
        authorizationService.requireAdministrador(authUserId, AuthorizedAction.LISTAR_USUARIOS);
        var tiendas = tiendaRepositoryPort.findAll();
        var centros = centroDistribucionRepositoryPort.obtenerTodos();

        var asignaciones = new HashMap<Long, AsignacionUbicacion>();
        for (var tienda : tiendas) {
            Long encargadoId = tienda.getEncargadoId();
            if (encargadoId != null) {
                asignaciones.put(encargadoId, AsignacionUbicacion.tienda(tienda));
            }
        }
        for (var centro : centros) {
            Long encargadoId = centro.getEncargadoId();
            if (encargadoId != null) {
                asignaciones.put(encargadoId, AsignacionUbicacion.cd(centro));
            }
        }

        return usuarioRepositoryPort.obtenerTodos().stream()
                .map(usuario -> {
                    AsignacionUbicacion asignacion = null;
                    if (usuario.esEncargadoTienda() || usuario.esEncargadoCD()) {
                        asignacion = asignaciones.get(usuario.getId());
                    }
                    return UsuarioResponse.from(usuario, asignacion);
                })
                .toList();
    }

    @Override
    public Optional<UsuarioResponse> obtenerUsuarioPorId(UUID authUserId, Long id) {
        Usuario solicitante = authorizationService.requireAuthenticated(authUserId);

        Usuario objetivo = usuarioRepositoryPort.obtenerPorId(id).orElse(null);
        if (objetivo == null) {
            return Optional.empty();
        }

        // Sólo un administrador puede consultar el detalle de otros usuarios;
        // cualquier usuario autenticado puede consultar el suyo propio.
        boolean esPropio = solicitante.getId() != null && solicitante.getId().equals(objetivo.getId());
        if (!esPropio && !solicitante.esAdministrador()) {
            throw new AccesoDenegadoException("No tiene permisos para consultar el detalle de otro usuario");
        }

        return Optional.of(UsuarioResponse.from(objetivo, buscarAsignacion(objetivo)));
    }

    @Override
    public Optional<UsuarioResponse> obtenerUsuarioPorAuthId(UUID authUserId) {
        Usuario solicitante = authorizationService.requireAuthenticated(authUserId);
        return usuarioRepositoryPort.obtenerPorAuthUserId(solicitante.getAuthUserId())
                .map(usuario -> UsuarioResponse.from(usuario, buscarAsignacion(usuario)));
    }

    /**
     * Resuelve la ubicación sólo para los roles que pueden tener una.
     * Administradores y planificadores no tienen asignación, así que se evitan
     * las dos consultas (tiendas y centros de distribución).
     */
    private AsignacionUbicacion buscarAsignacion(Usuario usuario) {
        if (usuario.esEncargadoTienda()) {
            return tiendaRepositoryPort.obtenerPorEncargadoId(usuario.getId()).stream()
                    .findFirst()
                    .map(AsignacionUbicacion::tienda)
                    .orElse(null);
        }
        if (usuario.esEncargadoCD()) {
            return centroDistribucionRepositoryPort.obtenerPorEncargadoId(usuario.getId()).stream()
                    .findFirst()
                    .map(AsignacionUbicacion::cd)
                    .orElse(null);
        }
        return null;
    }

    @Override
    public void eliminarUsuario(UUID authUserId, Long id) {
        authorizationService.requireAdministrador(authUserId, AuthorizedAction.ELIMINAR_USUARIO);
        Usuario usuarioAEliminar = usuarioRepositoryPort.obtenerPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

        if (authUserId.equals(usuarioAEliminar.getAuthUserId())) {
            throw new IllegalArgumentException("Un administrador no puede eliminarse a sí mismo");
        }

        if (usuarioAEliminar.esAdministrador()) {
            throw new AccesoDenegadoException("No está permitido eliminar a otro usuario con rol ADMINISTRADOR");
        }

        if (usuarioAEliminar.getAuthUserId() != null) {
            try {
                authPort.eliminarUsuarioAuth(usuarioAEliminar.getAuthUserId());
            } catch (SupabaseAuthException exception) {
                throw authErrorMapper.mapear(exception);
            }
        }

        usuarioRepositoryPort.eliminar(id);
    }

    /**
     * Invita al usuario en Supabase Auth traduciendo cualquier fallo técnico del
     * proveedor a un mensaje de negocio (la traducción vive en la aplicación).
     */
    private UUID invitarEnAuth(String email) {
        try {
            return authPort.invitar(email);
        } catch (SupabaseAuthException exception) {
            throw authErrorMapper.mapear(exception);
        }
    }

    @Override
    public UsuarioResponse actualizarRol(UUID authUserId, ActualizarRolCommand command) {
        authorizationService.requireAdministrador(authUserId, AuthorizedAction.ACTUALIZAR_ROL);

        Usuario usuario = usuarioRepositoryPort.obtenerPorId(command.usuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + command.usuarioId()));

        Rol nuevoRol = command.nuevoRol();

        if (usuario.esAdministrador()) {
            throw new AccesoDenegadoException(
                    "No está permitido cambiar el rol de un usuario con rol ADMINISTRADOR");
        }

        if (nuevoRol == Rol.ADMINISTRADOR) {
            throw new AccesoDenegadoException(
                    "No está permitido otorgar el rol ADMINISTRADOR");
        }

        usuario.setRol(nuevoRol);
        return UsuarioResponse.from(usuarioRepositoryPort.guardar(usuario), null);
    }
}
