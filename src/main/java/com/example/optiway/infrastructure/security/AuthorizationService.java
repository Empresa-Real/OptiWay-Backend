package com.example.optiway.infrastructure.security;

import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.application.service.AccesoDenegadoException;
import com.example.optiway.domain.model.Usuario;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Componente centralizado de autorización que valida la identidad y permisos
 * de los usuarios autenticados de manera uniforme, eliminando el boilerplate
 * repetido de verificación de roles en los servicios de aplicación.
 */
@Component
public class AuthorizationService {

    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public AuthorizationService(UsuarioRepositoryPort usuarioRepositoryPort) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    /**
     * Valida que el usuario autenticado exista y tenga rol ADMINISTRADOR.
     * El authUserId es obligatorio.
     *
     * @param authUserId identificador de autenticación (no puede ser null)
     * @param accion     descripción de la acción para el mensaje de error
     * @return el Usuario autenticado y autorizado
     */
    public Usuario requireAdministrador(UUID authUserId, String accion) {
        if (authUserId == null) {
            throw new AccesoDenegadoException("Autenticación requerida para " + accion);
        }
        Usuario usuario = usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)
                .orElseThrow(() -> new AccesoDenegadoException("Usuario no encontrado"));
        if (!usuario.esAdministrador()) {
            throw new AccesoDenegadoException("Solo un administrador puede " + accion);
        }
        return usuario;
    }

    /**
     * Valida que, si se proporciona authUserId, el usuario exista y sea administrador.
     * Útil para endpoints donde la autenticación es opcional pero, si está presente,
     * debe ser de administrador.
     *
     * @param authUserId identificador de autenticación (puede ser null)
     * @param accion     descripción de la acción para el mensaje de error
     * @return el Usuario autenticado, o null si no se proporcionó authUserId
     */
    public Usuario requireAdministradorIfPresent(UUID authUserId, String accion) {
        if (authUserId == null) {
            return null;
        }
        return requireAdministrador(authUserId, accion);
    }

    /**
     * Valida que el usuario autenticado exista (cualquier rol).
     *
     * @param authUserId identificador de autenticación (no puede ser null)
     * @return el Usuario autenticado
     */
    public Usuario requireAuthenticated(UUID authUserId) {
        if (authUserId == null) {
            throw new AccesoDenegadoException("Autenticación requerida");
        }
        return usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)
                .orElseThrow(() -> new AccesoDenegadoException("Usuario no encontrado"));
    }
}
