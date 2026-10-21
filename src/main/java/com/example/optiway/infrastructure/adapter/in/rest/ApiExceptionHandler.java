package com.example.optiway.infrastructure.adapter.in.rest;

import java.util.Objects;

import com.example.optiway.application.exception.AccesoDenegadoException;
import com.example.optiway.application.exception.InvitacionAuthException;
import com.example.optiway.application.exception.ProductoDuplicadoException;
import com.example.optiway.application.exception.RecursoNoEncontradoException;
import com.example.optiway.application.exception.StockInsuficienteException;
import com.example.optiway.application.exception.UsuarioDuplicadoException;
import com.example.optiway.infrastructure.adapter.in.rest.dto.ErrorResponse;
import com.example.optiway.infrastructure.security.AutenticacionRequeridaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(InvitacionAuthException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorResponse invitacionFallida(InvitacionAuthException exception) {
        return ErrorResponse.of(exception.getError(), exception.getMessage(), null);
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse accesoDenegado(AccesoDenegadoException exception) {
        return ErrorResponse.of(exception.getMessage());
    }

    @ExceptionHandler(AutenticacionRequeridaException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse autenticacionRequerida(AutenticacionRequeridaException exception) {
        return ErrorResponse.of(exception.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse accesoDenegadoSpring(AccessDeniedException exception) {
        return ErrorResponse.of("No tienes permisos suficientes para realizar esta acción.");
    }

    @ExceptionHandler(UsuarioDuplicadoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse usuarioDuplicado(UsuarioDuplicadoException exception) {
        return ErrorResponse.of(exception.getMessage());
    }

    @ExceptionHandler(ProductoDuplicadoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse productoDuplicado(ProductoDuplicadoException exception) {
        return ErrorResponse.of(exception.getMessage());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse recursoNoEncontrado(RecursoNoEncontradoException exception) {
        return ErrorResponse.of(exception.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> responseStatus(ResponseStatusException exception) {
        String mensaje = exception.getReason() != null ? exception.getReason() : "El recurso solicitado no fue encontrado.";
        return ResponseEntity.status(exception.getStatusCode()).body(ErrorResponse.of(mensaje));
    }

    @ExceptionHandler(StockInsuficienteException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse stockInsuficiente(StockInsuficienteException exception) {
        return ErrorResponse.of(exception.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse solicitudInvalida(RuntimeException exception) {
        return ErrorResponse.of(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse validacionFallida(MethodArgumentNotValidException exception) {
        String mensaje = exception.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("Los datos enviados contienen campos inválidos o incompletos.");
        return ErrorResponse.of(mensaje);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse mensajeNoLegible(HttpMessageNotReadableException exception) {
        // Si el fallo es un valor no permitido para un tipo enum (p. ej. un rol
        // inexistente), se informa con claridad en lugar del genérico de JSON.
        if (exception.getCause() instanceof InvalidFormatException cause
                && cause.getTargetType() != null && cause.getTargetType().isEnum()) {
            return ErrorResponse.of("El valor '" + cause.getValue() + "' no es válido para "
                    + cause.getPath().stream().map(ref -> ref.getFieldName()).filter(Objects::nonNull).findFirst().orElse("el campo")
                    + ". Valores permitidos: " + java.util.Arrays.toString(cause.getTargetType().getEnumConstants()));
        }
        return ErrorResponse.of("El cuerpo de la solicitud no tiene un formato JSON válido.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorResponse metodoNoPermitido(HttpRequestMethodNotSupportedException exception) {
        return ErrorResponse.of("El método HTTP utilizado no está permitido para esta ruta.");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ErrorResponse medioNoSoportado(HttpMediaTypeNotSupportedException exception) {
        return ErrorResponse.of("El tipo de contenido (Content-Type) no es compatible. Use application/json.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse rutaNoEncontrada(NoResourceFoundException exception) {
        return ErrorResponse.of("La ruta solicitada no existe en el servidor.");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse errorInesperado(Exception exception) {
        log.error("Error no controlado en el servidor: ", exception);
        return ErrorResponse.of("Ocurrió un error interno en el servidor. Por favor intente más tarde.");
    }
}