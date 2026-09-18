package com.example.optiway.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.optiway.application.port.in.CrearUsuarioUseCase;
import com.example.optiway.application.port.in.EliminarUsuarioUseCase;
import com.example.optiway.application.port.in.ObtenerUsuarioUseCase;
import com.example.optiway.application.port.in.ObtenerUsuariosUseCase;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.domain.model.Usuario;

@Service
public class UsuarioService implements CrearUsuarioUseCase, ObtenerUsuariosUseCase,
    ObtenerUsuarioUseCase, EliminarUsuarioUseCase {
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public UsuarioService(UsuarioRepositoryPort usuarioRepositoryPort) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    @Override
    public Usuario crearUsuario(Usuario usuario) {
        if(usuario.getNombre() == null || usuario.getNombre().isEmpty()) {
            throw new IllegalArgumentException("El nombre del usuario no puede estar vacio");
        }
        if(usuario.getEmail() == null || usuario.getEmail().isEmpty()) {
            throw new IllegalArgumentException("El email del usuario no puede estar vacio");
        }
        return usuarioRepositoryPort.guardar(usuario);
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
