package com.example.optiway.infrastructure.adapter.out.auth;

/**
 * Excepción técnica que representa un fallo al comunicarse con Supabase Auth.
 * Transporta los datos crudos de la respuesta (código HTTP y cuerpo) para que la
 * capa de aplicación sea quien decida cómo interpretarlos y qué mensaje mostrar
 * al usuario. El adaptador no construye mensajes de negocio, pero sí extrae el
 * mensaje natural que envía el proveedor ({@code msg}) para que la aplicación no
 * tenga que interpretar el JSON de Supabase.
 */
public class SupabaseAuthException extends RuntimeException {

    private final Integer statusCode;
    private final String responseBody;
    private final String providerMessage;

    public SupabaseAuthException(String message, Integer statusCode, String responseBody) {
        this(message, statusCode, responseBody, null);
    }

    public SupabaseAuthException(String message, Integer statusCode, String responseBody,
            String providerMessage) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.providerMessage = providerMessage;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    /**
     * Mensaje natural enviado por Supabase (campo {@code msg} del cuerpo de error).
     * Puede ser {@code null} si la respuesta no traía un mensaje legible.
     */
    public String getProviderMessage() {
        return providerMessage;
    }
}
