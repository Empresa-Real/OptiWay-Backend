package com.example.optiway.infrastructure.adapter.in.rest.docs;

import com.example.optiway.infrastructure.adapter.in.rest.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Documents invalid path/query/body input for an operation that accepts client input. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(responseCode = "400", description = "Solicitud inválida: parámetro, formato o validación del cuerpo",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
public @interface ApiBadRequestResponse {
}
