package com.example.optiway.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estructura estandarizada de respuesta de error de la API")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    @Schema(description = "Código o mensaje del error original (técnico)", example = "METHOD_ARGUMENT_NOT_VALID")
    String error,

    @Schema(description = "Mensaje amigable en español para el cliente", example = "El nombre del producto ya existe.")
    String message,

    @Schema(description = "Código de estado HTTP", example = "400")
    Integer status
) {
    public static ErrorResponse of(String error, String message, Integer status) {
        return new ErrorResponse(error, message, status);
    }

    public static ErrorResponse of(String mensaje, Integer status) {
        String msgLimpio = limpiarMensaje(mensaje);
        return new ErrorResponse(msgLimpio, msgLimpio, status);
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
