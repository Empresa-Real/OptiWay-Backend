package com.example.optiway.infrastructure.security;

import com.example.optiway.application.port.out.AuthorizationPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.application.exception.AccesoDenegadoException;
import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.domain.model.Tienda;
import com.example.optiway.domain.model.Usuario;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Componente centralizado de autorización que valida la identidad y permisos
 * de los usuarios autenticados de manera uniforme, eliminando el boilerplate
 * repetido de verificación de roles en los servicios de aplicación.
 *
 * <p>Implementa {@link AuthorizationPort}, de modo que los servicios dependen de
 * la abstracción y no de esta clase concreta.
 */
@Component
public class AuthorizationService implements AuthorizationPort {

    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public AuthorizationService(UsuarioRepositoryPort usuarioRepositoryPort) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    /**
     * Valida que el usuario autenticado exista y tenga rol ADMINISTRADOR.
     * El authUserId es obligatorio.
     *
     * @param authUserId identificador de autenticación (no puede ser null)
     * @param accion     acción autorizada (define el mensaje de error)
     * @return el Usuario autenticado y autorizado
     */
    @Override
    public Usuario requireAdministrador(UUID authUserId, AccionAutorizada accion) {
        if (authUserId == null) {
            throw new AccesoDenegadoException("Autenticación requerida para " + accion.descripcion());
        }
        Usuario usuario = usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)
                .orElseThrow(() -> new AccesoDenegadoException("Usuario no encontrado"));
        if (!usuario.esAdministrador()) {
            throw new AccesoDenegadoException("Solo un administrador puede " + accion.descripcion());
        }
        return usuario;
    }

    /**
     * Valida que el usuario autenticado pueda gestionar el dominio logístico sin
     * restricción de ubicación (ADMINISTRADOR o PLANIFICADOR). No cubre la gestión
     * de usuarios, que sigue siendo exclusiva del ADMINISTRADOR.
     *
     * @param authUserId identificador de autenticación (no puede ser null)
     * @param accion     acción autorizada (define el mensaje de error)
     * @return el Usuario autenticado y autorizado
     */
    @Override
    public Usuario requireGestionLogistica(UUID authUserId, AccionAutorizada accion) {
        Usuario usuario = requireAuthenticated(authUserId);
        if (!usuario.puedeGestionarTodo()) {
            throw new AccesoDenegadoException("No tiene permisos para " + accion.descripcion());
        }
        return usuario;
    }

    /**
     * Valida que el usuario autenticado exista (cualquier rol).
     *
     * @param authUserId identificador de autenticación (no puede ser null)
     * @return el Usuario autenticado
     */
    @Override
    public Usuario requireAuthenticated(UUID authUserId) {
        if (authUserId == null) {
            throw new AccesoDenegadoException("Autenticación requerida");
        }
        return usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)
                .orElseThrow(() -> new AccesoDenegadoException("Usuario no encontrado"));
    }

    /**
     * Valida que el usuario autenticado tenga permisos para operar o consultar una tienda
     * (debe poder gestionar todo el dominio logístico o ser el Encargado asignado a esa tienda).
     *
     * @param authUserId identificador de autenticación
     * @param tienda     tienda sobre la que se operará
     * @param accion     acción autorizada (define el mensaje de error)
     * @return el Usuario autenticado y autorizado
     */
    @Override
    public Usuario requirePermisoTienda(UUID authUserId, Tienda tienda, AccionAutorizada accion) {
        Usuario usuario = requireAuthenticated(authUserId);
        if (!usuario.puedeOperarTienda(tienda)) {
            throw new AccesoDenegadoException("No tiene permisos para " + accion.descripcion());
        }
        return usuario;
    }

    /**
     * Valida que el usuario autenticado tenga permisos para operar o consultar un Centro de Distribución
     * (debe poder gestionar todo el dominio logístico o ser el Encargado asignado a ese CD).
     *
     * @param authUserId         identificador de autenticación
     * @param centroDistribucion centro de distribución sobre el que se operará
     * @param accion             acción autorizada (define el mensaje de error)
     * @return el Usuario autenticado y autorizado
     */
     @Override
     public Usuario requirePermisoCentroDistribucion(UUID authUserId, CentroDistribucion centroDistribucion, AccionAutorizada accion) {
         Usuario usuario = requireAuthenticated(authUserId);
         if (!usuario.puedeOperarCentroDistribucion(centroDistribucion)) {
             throw new AccesoDenegadoException("No tiene permisos para " + accion.descripcion());
         }
         return usuario;
     }
}
