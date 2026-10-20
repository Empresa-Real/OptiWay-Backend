package com.example.optiway.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.optiway.application.port.in.ActualizarRolCommand;
import com.example.optiway.application.port.in.ActualizarRolUsuarioUseCase;
import com.example.optiway.application.port.in.CrearUsuarioCommand;
import com.example.optiway.application.port.in.CrearUsuarioUseCase;
import com.example.optiway.application.port.in.EliminarUsuarioUseCase;
import com.example.optiway.application.port.in.ObtenerUsuarioUseCase;
import com.example.optiway.application.port.in.ObtenerUsuariosUseCase;
import com.example.optiway.application.port.in.dto.UsuarioResponse;
import com.example.optiway.application.port.out.AuthPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.Usuario;
import com.example.optiway.infrastructure.security.AuthorizationService;

@Service
public class UsuarioService implements CrearUsuarioUseCase, ObtenerUsuariosUseCase,
    ObtenerUsuarioUseCase, EliminarUsuarioUseCase, ActualizarRolUsuarioUseCase {
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final AuthPort authPort;
    private final AuthorizationService authorizationService;

    public UsuarioService(
            UsuarioRepositoryPort usuarioRepositoryPort,
            AuthPort authPort,
            AuthorizationService authorizationService) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.authPort = authPort;
        this.authorizationService = authorizationService;
    }

    @Override
    public UsuarioResponse crearUsuario(UUID authUserId, CrearUsuarioCommand command) {
        authorizationService.requireAdministrador(authUserId, "crear usuarios");

        String nombre = command.nombre().trim();
        String email = command.email().trim().toLowerCase();
        Rol rol = Rol.valueOf(command.rol().toUpperCase());

        if (usuarioRepositoryPort.existePorEmail(email)) {
            throw new UsuarioDuplicadoException("El correo ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setRol(rol);
        usuario.setCreadoPor(authUserId);
        usuario.setAuthUserId(authPort.invitar(email));
        return UsuarioResponse.from(usuarioRepositoryPort.guardar(usuario));
    }

    @Override
    public List<UsuarioResponse> obtenerUsuarios(UUID authUserId) {
        authorizationService.requireAdministrador(authUserId, "consultar la lista de usuarios");
        return usuarioRepositoryPort.obtenerTodos().stream()
                .map(UsuarioResponse::from)
                .toList();
    }

    @Override
    public Optional<UsuarioResponse> obtenerUsuarioPorId(UUID authUserId, Long id) {
        authorizationService.requireAuthenticated(authUserId);
        return usuarioRepositoryPort.obtenerPorId(id).map(UsuarioResponse::from);
    }

    @Override
    public Optional<UsuarioResponse> obtenerUsuarioPorAuthId(UUID authUserId) {
        return usuarioRepositoryPort.obtenerPorAuthUserId(authUserId).map(UsuarioResponse::from);
    }

    @Override
    public void eliminarUsuario(UUID authUserId, Long id) {
        authorizationService.requireAdministrador(authUserId, "eliminar usuarios");
        Usuario usuarioAEliminar = usuarioRepositoryPort.obtenerPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

        if (authUserId.equals(usuarioAEliminar.getAuthUserId())) {
            throw new IllegalArgumentException("Un administrador no puede eliminarse a sí mismo");
        }

        if (usuarioAEliminar.esAdministrador()) {
            throw new AccesoDenegadoException("No está permitido eliminar a otro usuario con rol ADMINISTRADOR");
        }

        if (usuarioAEliminar.getAuthUserId() != null) {
            authPort.eliminarUsuarioAuth(usuarioAEliminar.getAuthUserId());
        }

        usuarioRepositoryPort.eliminar(id);
    }

    @Override
    public UsuarioResponse actualizarRol(UUID authUserId, ActualizarRolCommand command) {
        authorizationService.requireAdministrador(authUserId, "actualizar roles de usuario");

        Usuario usuario = usuarioRepositoryPort.obtenerPorId(command.usuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + command.usuarioId()));

        Rol nuevoRol = Rol.valueOf(command.nuevoRol().toUpperCase());
        usuario.setRol(nuevoRol);
        return UsuarioResponse.from(usuarioRepositoryPort.guardar(usuario));
    }
}
