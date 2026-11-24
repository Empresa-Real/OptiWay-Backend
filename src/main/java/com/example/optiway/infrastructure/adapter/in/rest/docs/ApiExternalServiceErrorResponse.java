package com.example.optiway.infrastructure.adapter.in.rest.docs;

import com.example.optiway.infrastructure.adapter.in.rest.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Documents a gateway error returned when the external authentication provider fails. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(responseCode = "502", description = "El proveedor externo de autenticación no pudo completar la operación",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
public @interface ApiExternalServiceErrorResponse {
}
