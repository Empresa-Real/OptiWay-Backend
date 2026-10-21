package com.example.optiway.infrastructure.adapter.out.auth;

/**
 * Excepción técnica que representa un fallo al comunicarse con Supabase Auth.
 * Transporta los datos crudos de la respuesta (código HTTP y cuerpo) para que la
 * capa de aplicación sea quien decida cómo interpretarlos y qué mensaje mostrar
 * al usuario. El adaptador no construye mensajes de negocio.
 */
public class SupabaseAuthException extends RuntimeException {

    private final Integer statusCode;
    private final String responseBody;

    public SupabaseAuthException(String message, Integer statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
