package com.example.optiway.application.port.out;

import com.example.optiway.domain.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepositoryPort {

    Usuario guardar(Usuario usuario);

    List<Usuario> obtenerTodos();

    Optional<Usuario> obtenerPorID(Long id);

    Void eliminar(Long id);
}
