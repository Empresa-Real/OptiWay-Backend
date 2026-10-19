package com.example.optiway.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estructura estandarizada de respuesta de error de la API")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        @Schema(description = "Mensaje del error en español para el cliente", example = "El nombre del producto ya existe.")
        String error,

        @Schema(description = "Mensaje idéntico para compatibilidad con librerías frontend", example = "El nombre del producto ya existe.")
        String message,

        @Schema(description = "Código de estado HTTP o sub-código de error", example = "400")
        Integer status
) {
    public static ErrorResponse of(String mensaje) {
        String msgLimpio = limpiarMensaje(mensaje);
        return new ErrorResponse(msgLimpio, msgLimpio, null);
    }

    public static ErrorResponse of(String mensaje, Integer status) {
        String msgLimpio = limpiarMensaje(mensaje);
        return new ErrorResponse(msgLimpio, msgLimpio, status);
    }

    private static String limpiarMensaje(String raw) {
        if (raw == null || raw.isBlank()) {
            return "Ocurrió un error al procesar la solicitud.";
        }
        String texto = raw.trim();
        // Si el texto contiene JSON incrustado (ej. "...: {\"code\": 400}"), descartar el fragmento JSON técnico
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
