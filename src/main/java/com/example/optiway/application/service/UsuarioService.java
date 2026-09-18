package com.example.optiway.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.optiway.application.port.in.CrearUsuarioUseCase;
import com.example.optiway.application.port.in.EliminarUsuarioUseCase;
import com.example.optiway.application.port.in.ObtenerUsuarioUseCase;
import com.example.optiway.application.port.in.ObtenerUsuariosUseCase;
import com.example.optiway.application.port.out.InvitarUsuarioPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.Usuario;

@Service
public class UsuarioService implements CrearUsuarioUseCase, ObtenerUsuariosUseCase,
    ObtenerUsuarioUseCase, EliminarUsuarioUseCase {
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final InvitarUsuarioPort invitarUsuarioPort;

    public UsuarioService(
            UsuarioRepositoryPort usuarioRepositoryPort,
            InvitarUsuarioPort invitarUsuarioPort) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.invitarUsuarioPort = invitarUsuarioPort;
    }

    @Override
    public Usuario crearUsuario(Usuario usuario, UUID authUserId) {
        if (authUserId == null || !esAdministrador(authUserId)) {
            throw new AccesoDenegadoException("Solo un administrador puede crear usuarios");
        }

        if(usuario.getNombre() == null || usuario.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del usuario no puede estar vacio");
        }
        if(usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new IllegalArgumentException("El email del usuario no puede estar vacio");
        }

        String email = usuario.getEmail().trim();
        if (usuarioRepositoryPort.existePorEmail(email)) {
            throw new UsuarioDuplicadoException("El correo ya esta registrado");
        }

        if (usuario.getRol() == null) {
            throw new IllegalArgumentException("El rol no es valido");
        }

        usuario.setEmail(email);
        usuario.setCreadoPor(authUserId);
        usuario.setAuthUserId(invitarUsuarioPort.invitar(email));
        return usuarioRepositoryPort.guardar(usuario);
    }

    private boolean esAdministrador(UUID authUserId) {
        return usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)
                .map(Usuario::getRol)
                .filter(Rol.ADMINISTRADOR::equals)
                .isPresent();
    }

    @Override
    public List<Usuario> obtenerUsuarios() {
        return usuarioRepositoryPort.obtenerTodos();
    }

    @Override
    public Optional<Usuario> obtenerUsuario(Long id) {
        return usuarioRepositoryPort.obtenerPorID(id);
    }

    @Override
    public Optional<Usuario> obtenerUsuarioPorAuthId(UUID authUserId) {
        return usuarioRepositoryPort.obtenerPorAuthUserId(authUserId);
    }

    @Override
    public void eliminarUsuario(Long id) {
        usuarioRepositoryPort.eliminar(id);
    }
}
