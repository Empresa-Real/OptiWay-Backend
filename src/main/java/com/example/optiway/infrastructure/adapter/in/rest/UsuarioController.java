package com.example.optiway.infrastructure.adapter.in.rest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
import com.example.optiway.domain.model.Rol;
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
            @RequestBody Usuario usuario,
            @AuthenticationPrincipal Jwt jwt) {

        return crearUsuarioUseCase.crearUsuario(
            usuario,
            UUID.fromString(jwt.getSubject()));
    }

    @GetMapping
    public List<Usuario> obtenerUsuarios(@AuthenticationPrincipal Jwt jwt) {
        return obtenerUsuariosUseCase.obtenerUsuarios(UUID.fromString(jwt.getSubject()));
    }

    @GetMapping("/{id}")
    public Optional<Usuario> obtenerUsuario(
            @PathVariable Long id) {

        return obtenerUsuarioUseCase.obtenerUsuario(id);
    }

    @GetMapping("/me")
    public Usuario obtenerUsuarioAutenticado(@AuthenticationPrincipal Jwt jwt) {
        return obtenerUsuarioUseCase.obtenerUsuarioPorAuthId(UUID.fromString(jwt.getSubject()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarUsuario(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        eliminarUsuarioUseCase.eliminarUsuario(id, UUID.fromString(jwt.getSubject()));
    }

    @PatchMapping("/{id}/rol")
    public Usuario actualizarRol(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal Jwt jwt) {

        String nuevoRolStr = body != null ? body.get("rol") : null;
        if (nuevoRolStr == null || nuevoRolStr.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nuevo rol es obligatorio.");
        }
        Rol nuevoRol;
        try {
            nuevoRol = Rol.valueOf(nuevoRolStr);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no válido: " + nuevoRolStr);
        }
        return actualizarRolUsuarioUseCase.actualizarRol(id, nuevoRol, UUID.fromString(jwt.getSubject()));
    }
}