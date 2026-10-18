package com.example.optiway.infrastructure.adapter.in.rest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.optiway.infrastructure.adapter.in.rest.dto.ActualizarRolRequest;
import com.example.optiway.infrastructure.adapter.in.rest.dto.CrearUsuarioRequest;
import com.example.optiway.infrastructure.security.AuthUserId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.optiway.application.port.in.ActualizarRolUsuarioUseCase;
import com.example.optiway.application.port.in.CrearUsuarioUseCase;
import com.example.optiway.application.port.in.EliminarUsuarioUseCase;
import com.example.optiway.application.port.in.ObtenerUsuarioUseCase;
import com.example.optiway.application.port.in.ObtenerUsuariosUseCase;
import com.example.optiway.domain.model.Usuario;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final CrearUsuarioUseCase crearUsuarioUseCase;
    private final ObtenerUsuariosUseCase obtenerUsuariosUseCase;
    private final ObtenerUsuarioUseCase obtenerUsuarioUseCase;
    private final EliminarUsuarioUseCase eliminarUsuarioUseCase;
    private final ActualizarRolUsuarioUseCase actualizarRolUsuarioUseCase;

    public UsuarioController(
            CrearUsuarioUseCase crearUsuarioUseCase,
            ObtenerUsuariosUseCase obtenerUsuariosUseCase,
            ObtenerUsuarioUseCase obtenerUsuarioUseCase,
            EliminarUsuarioUseCase eliminarUsuarioUseCase,
            ActualizarRolUsuarioUseCase actualizarRolUsuarioUseCase) {

        this.crearUsuarioUseCase = crearUsuarioUseCase;
        this.obtenerUsuariosUseCase = obtenerUsuariosUseCase;
        this.obtenerUsuarioUseCase = obtenerUsuarioUseCase;
        this.eliminarUsuarioUseCase = eliminarUsuarioUseCase;
        this.actualizarRolUsuarioUseCase = actualizarRolUsuarioUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario crearUsuario(
            @Valid @RequestBody CrearUsuarioRequest request,
            @AuthUserId UUID authUserId) {

        Usuario usuario = request != null ? request.toDomain() : null;
        return crearUsuarioUseCase.crearUsuario(usuario, authUserId);
    }

    @GetMapping
    public List<Usuario> obtenerUsuarios(@AuthUserId UUID authUserId) {
        return obtenerUsuariosUseCase.obtenerUsuarios(authUserId);
    }

    @GetMapping("/{id}")
    public Optional<Usuario> obtenerUsuario(
            @PathVariable Long id) {

        return obtenerUsuarioUseCase.obtenerUsuario(id);
    }

    @GetMapping("/me")
    public Usuario obtenerUsuarioAutenticado(@AuthUserId UUID authUserId) {
        return obtenerUsuarioUseCase.obtenerUsuarioPorAuthId(authUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarUsuario(
            @PathVariable Long id,
            @AuthUserId UUID authUserId) {

        eliminarUsuarioUseCase.eliminarUsuario(id, authUserId);
    }

    @PatchMapping("/{id}/rol")
    public Usuario actualizarRol(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) ActualizarRolRequest request,
            @AuthUserId UUID authUserId) {

        if (request == null || request.rol() == null) {
            throw new IllegalArgumentException("El nuevo rol es obligatorio.");
        }
        return actualizarRolUsuarioUseCase.actualizarRol(id, request.rol(), authUserId);
    }
}