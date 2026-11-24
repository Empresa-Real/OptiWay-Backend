package com.example.optiway.infrastructure.adapter.in.rest.docs;

import com.example.optiway.infrastructure.adapter.in.rest.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Documents unsupported request content for endpoints that accept JSON bodies. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(responseCode = "415", description = "Content-Type no soportado; se espera application/json",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
public @interface ApiUnsupportedMediaTypeResponse {
}
