package com.example.optiway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.example.optiway.application.exception.RecursoNoEncontradoException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.example.optiway.infrastructure.adapter.in.rest.ApiExceptionHandler;
import com.example.optiway.infrastructure.adapter.in.rest.dto.ErrorResponse;

/**
 * Pruebas del mapeo de errores de conversión de parámetros de ruta.
 * Un id no numérico (por ejemplo un UUID) debe responder 400, no 500.
 */
class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    @DisplayName("Un path variable no convertible a Long devuelve un error de solicitud inválida")
    void pathVariableNoNumerico_mensajeDeSolicitudInvalida() {
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "00000000-0000-0000-0000-000000000000", Long.class, "id", null,
                new NumberFormatException("For input string: \"00000000-0000-0000-0000-000000000000\""));

        ErrorResponse response = handler.parametroConTipoInvalido(exception);

        assertEquals(response.error(), response.message());
        assertTrue(response.message().contains("'id'"));
        assertTrue(response.message().contains("número entero"));
    }

    @Test
    @DisplayName("Un query param requerido ausente devuelve un error de solicitud inválida")
    void parametroRequeridoAusente_mensajeDeSolicitudInvalida() {
        MissingServletRequestParameterException exception = new MissingServletRequestParameterException(
                "tiendaId", "Long");

        ErrorResponse response = handler.parametroRequeridoFaltante(exception);

        assertEquals(response.error(), response.message());
        assertTrue(response.message().contains("'tiendaId'"));
    }

    @Test
    @DisplayName("Un recurso inexistente debe estar configurado para responder HTTP 404")
    void recursoNoEncontrado_debeResponderHttp404() throws NoSuchMethodException {
        RecursoNoEncontradoException exception =
                new RecursoNoEncontradoException("Producto no encontrado con ID: 999");

        ErrorResponse response = handler.recursoNoEncontrado(exception);

        assertEquals("Producto no encontrado con ID: 999", response.message());

        ResponseStatus responseStatus = ApiExceptionHandler.class
                .getMethod("recursoNoEncontrado", RecursoNoEncontradoException.class)
                .getAnnotation(ResponseStatus.class);

        assertEquals(HttpStatus.NOT_FOUND, responseStatus.value());
    }

}
