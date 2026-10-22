package com.example.optiway.infrastructure.security;

/**
 * Excepción lanzada cuando un endpoint protegido recibe una petición sin una
 * identidad válida. A diferencia de {@code AccesoDenegadoException} (403, el
 * usuario está autenticado pero no tiene permiso), esta excepción representa la
 * ausencia o invalidez de la identidad y se traduce en un 401.
 */
public class AutenticacionRequeridaException extends RuntimeException {

    public AutenticacionRequeridaException(String message) {
        super(message);
    }
}
