package com.example.optiway.infrastructure.adapter.in.rest.docs;

import com.example.optiway.infrastructure.adapter.in.rest.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Documents a business conflict response for an endpoint that can reject a state transition. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(responseCode = "409", description = "Conflicto con el estado o las reglas actuales del recurso",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
public @interface ApiConflictResponse {
}
