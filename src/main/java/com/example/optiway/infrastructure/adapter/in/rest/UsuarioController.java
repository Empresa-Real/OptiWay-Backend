package com.example.optiway.infrastructure.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import com.example.optiway.application.port.in.ActualizarRolUsuarioUseCase;
import com.example.optiway.application.port.in.CrearUsuarioUseCase;
import com.example.optiway.application.port.in.EliminarUsuarioUseCase;
import com.example.optiway.application.port.in.ObtenerUsuarioUseCase;
import com.example.optiway.application.port.in.ObtenerUsuariosUseCase;
import com.example.optiway.application.port.in.dto.UsuarioResponse;
import com.example.optiway.infrastructure.adapter.in.rest.dto.ActualizarRolRequest;
import com.example.optiway.infrastructure.adapter.in.rest.dto.CrearUsuarioRequest;
import com.example.optiway.infrastructure.security.AuthUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Endpoints para administración de usuarios, asignación de roles y consulta de perfil")
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
    @Operation(summary = "Crear nuevo usuario", description = "Registra un usuario en la plataforma asignándole un rol inicial. Requiere rol ADMINISTRADOR.")
    public UsuarioResponse crearUsuario(
            @AuthUserId UUID authUserId,
            @Valid @RequestBody CrearUsuarioRequest request) {
        return crearUsuarioUseCase.crearUsuario(authUserId, request.toCommand());
    }

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Retorna el listado de todos los usuarios registrados. Requiere rol ADMINISTRADOR.")
    public List<UsuarioResponse> obtenerUsuarios(@AuthUserId UUID authUserId) {
        return obtenerUsuariosUseCase.obtenerUsuarios(authUserId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID", description = "Consulta un usuario específico por su ID interno. Requiere autenticación.")
    public UsuarioResponse obtenerUsuario(@AuthUserId UUID authUserId, @PathVariable Long id) {
        return obtenerUsuarioUseCase.obtenerUsuarioPorId(authUserId, id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener perfil autenticado", description = "Consulta los datos y rol del usuario correspondiente al token JWT actual.")
    public UsuarioResponse obtenerUsuarioAutenticado(@AuthUserId UUID authUserId) {
        return obtenerUsuarioUseCase.obtenerUsuarioPorAuthId(authUserId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil no encontrado"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar usuario", description = "Elimina lógicamente o físicamente un usuario del sistema. Requiere rol ADMINISTRADOR.")
    public void eliminarUsuario(
            @AuthUserId UUID authUserId,
            @PathVariable Long id) {
        eliminarUsuarioUseCase.eliminarUsuario(authUserId, id);
    }

    @PatchMapping("/{id}/rol")
    @Operation(summary = "Actualizar rol de usuario", description = "Modifica el rol de un usuario existente. Requiere rol ADMINISTRADOR.")
    public UsuarioResponse actualizarRol(
            @AuthUserId UUID authUserId,
            @PathVariable Long id,
            @Valid @RequestBody ActualizarRolRequest request) {
        return actualizarRolUsuarioUseCase.actualizarRol(authUserId, request.toCommand(id));
    }
}
