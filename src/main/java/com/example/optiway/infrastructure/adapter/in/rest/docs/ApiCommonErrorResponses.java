package com.example.optiway.infrastructure.adapter.in.rest.docs;

import com.example.optiway.infrastructure.adapter.in.rest.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Respuestas de error comunes a los endpoints protegidos de la API.
 * Los errores de negocio específicos se declaran directamente en cada operación.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "Autenticación requerida: token ausente o inválido",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Usuario autenticado sin permisos para esta operación",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error inesperado del servidor",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public @interface ApiCommonErrorResponses {
}
