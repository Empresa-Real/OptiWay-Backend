package com.example.optiway.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.optiway.domain.model.Usuario;

public interface UsuarioRepositoryPort {

    Usuario guardar(Usuario usuario);

    default Usuario save(Usuario usuario) {
        return guardar(usuario);
    }

    List<Usuario> obtenerTodos();

    Optional<Usuario> obtenerPorID(Long id);

    boolean existePorEmail(String email);

    Optional<Usuario> obtenerPorEmail(String email);

    Optional<Usuario> obtenerPorAuthUserId(UUID authUserId);

    Void eliminar(Long id);
}
