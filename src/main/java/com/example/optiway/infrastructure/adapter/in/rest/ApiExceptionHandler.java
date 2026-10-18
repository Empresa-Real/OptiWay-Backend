package com.example.optiway.infrastructure.adapter.in.rest;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.optiway.application.service.AccesoDenegadoException;
import com.example.optiway.application.service.InvitacionAuthException;
import com.example.optiway.application.service.UsuarioDuplicadoException;
import com.example.optiway.application.service.ProductoDuplicadoException;

import com.example.optiway.application.service.StockInsuficienteException;
import com.example.optiway.application.service.RecursoNoEncontradoException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvitacionAuthException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public Map<String, String> invitacionFallida(InvitacionAuthException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> accesoDenegado(AccesoDenegadoException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(UsuarioDuplicadoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> usuarioDuplicado(UsuarioDuplicadoException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(ProductoDuplicadoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> productoDuplicado(ProductoDuplicadoException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> recursoNoEncontrado(RecursoNoEncontradoException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(StockInsuficienteException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> stockInsuficiente(StockInsuficienteException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> solicitudInvalida(IllegalArgumentException exception) {
        return Map.of("error", exception.getMessage());
    }
}