package com.example.optiway.infraestructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.domain.model.Usuario;

@Component
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {
    private final UsuarioJpaRepository usuarioJpaRepository;

    public UsuarioRepositoryAdapter (UsuarioJpaRepository usuarioJpaRepository){
        this.usuarioJpaRepository = usuarioJpaRepository;
    }

    @Override
    public Usuario guardar(Usuario usuario) {

        UsuarioJpaEntity entity = toEntity(usuario);
        
        UsuarioJpaEntity saved =
                usuarioJpaRepository.save(entity);

        return toDomain(saved);
    }

    @Override
    public List<Usuario> obtenerTodos() {

        return usuarioJpaRepository.findAll()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Usuario> obtenerPorID(Long id) {

        return usuarioJpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public boolean existePorEmail(String email) {
        return usuarioJpaRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public Optional<Usuario> obtenerPorEmail(String email) {
        return usuarioJpaRepository.findByEmailIgnoreCase(email)
                .map(this::toDomain);
    }

    @Override
    public Void eliminar(Long id) {

        usuarioJpaRepository.deleteById(id);
        return null;
    }

    private UsuarioJpaEntity toEntity(Usuario usuario) {

        return new UsuarioJpaEntity(
                usuario.getId(),
                usuario.getNombre(),
            usuario.getEmail(),
                usuario.getRol(),
                usuario.getCreadoPor()
        );
    }

    private Usuario toDomain(UsuarioJpaEntity entity) {

        return new Usuario(
                entity.getId(),
                entity.getNombre(),
            entity.getEmail(),
                entity.getRol(),
                entity.getCreadoPor()
        );
    }
}