package com.example.optiway.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.optiway.application.port.in.CrearUsuarioUseCase;
import com.example.optiway.application.port.in.EliminarUsuarioUseCase;
import com.example.optiway.application.port.in.ObtenerUsuarioUseCase;
import com.example.optiway.application.port.in.ObtenerUsuariosUseCase;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.domain.model.Usuario;
import com.example.optiway.domain.model.Rol;

@Service
public class UsuarioService implements CrearUsuarioUseCase, ObtenerUsuariosUseCase,
    ObtenerUsuarioUseCase, EliminarUsuarioUseCase {
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public UsuarioService(UsuarioRepositoryPort usuarioRepositoryPort) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    @Override
    public Usuario crearUsuario(Usuario usuario, String emailAdministrador, UUID authUserId) {
        if (emailAdministrador == null || authUserId == null || !esAdministrador(emailAdministrador)) {
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
        return usuarioRepositoryPort.guardar(usuario);
    }

    private boolean esAdministrador(String email) {
        return usuarioRepositoryPort.obtenerPorEmail(email)
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
    public void eliminarUsuario(Long id) {
        usuarioRepositoryPort.eliminar(id);
    }
}
