package com.example.optiway.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estructura estandarizada de respuesta de error de la API")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    @Schema(description = "Mensaje natural del proveedor o descripción técnica del error", example = "email rate limit exceeded")
    String error,

    @Schema(description = "Mensaje amigable en español para el cliente", example = "El nombre del producto ya existe.")
    String message,

    @Schema(description = "Código de error del proveedor (null si no lo envía)", example = "429")
    Integer code
) {
    public static ErrorResponse of(String error, String message, Integer code) {
        return new ErrorResponse(error, message, code);
    }

    public static ErrorResponse of(String mensaje) {
        String msgLimpio = limpiarMensaje(mensaje);
        return new ErrorResponse(msgLimpio, msgLimpio, null);
    }

    private static String limpiarMensaje(String raw) {
        if (raw == null || raw.isBlank()) {
            return "Ocurrió un error al procesar la solicitud.";
        }
        String texto = raw.trim();
        if (texto.contains("{") && texto.contains("}")) {
            int inicioJson = texto.indexOf("{");
            String prefijo = texto.substring(0, inicioJson).trim();
            if (prefijo.endsWith(":")) {
                prefijo = prefijo.substring(0, prefijo.length() - 1).trim();
            }
            if (!prefijo.isBlank()) {
                return prefijo;
            }
        }
        return texto;
    }
}
