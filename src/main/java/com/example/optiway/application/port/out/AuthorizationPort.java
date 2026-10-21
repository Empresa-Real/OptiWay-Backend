package com.example.optiway.application.port.out;

import java.util.UUID;

import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.domain.model.Tienda;
import com.example.optiway.domain.model.Usuario;
import com.example.optiway.infrastructure.security.AccionAutorizada;

/**
 * Puerto de salida para la autorización. Los servicios de aplicación dependen de
 * esta abstracción y no de la implementación concreta (que vive en
 * infraestructura), de modo que un cambio en el modelo de autorización no obliga
 * a modificar cada servicio.
 */
public interface AuthorizationPort {

    /**
     * Exige que el usuario autenticado sea ADMINISTRADOR.
     *
     * @return el Usuario autenticado y autorizado
     */
    Usuario requireAdministrador(UUID authUserId, AccionAutorizada accion);

    /**
     * Exige que el usuario autenticado pueda gestionar el dominio logístico
     * (ADMINISTRADOR o PLANIFICADOR).
     *
     * @return el Usuario autenticado y autorizado
     */
    Usuario requireGestionLogistica(UUID authUserId, AccionAutorizada accion);

    /**
     * Exige únicamente que exista una identidad autenticada válida.
     *
     * @return el Usuario autenticado
     */
    Usuario requireAuthenticated(UUID authUserId);

    /**
     * Exige permiso para operar la tienda indicada (gestión global o encargado asignado).
     *
     * @return el Usuario autenticado y autorizado
     */
    Usuario requirePermisoTienda(UUID authUserId, Tienda tienda, AccionAutorizada accion);

    /**
     * Exige permiso para operar el centro de distribución indicado (gestión global o encargado asignado).
     *
     * @return el Usuario autenticado y autorizado
     */
    Usuario requirePermisoCentroDistribucion(UUID authUserId, CentroDistribucion centroDistribucion,
            AccionAutorizada accion);
}
