package com.example.optiway.infraestructure.adapter.in.rest;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.optiway.application.service.UsuarioDuplicadoException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(UsuarioDuplicadoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> usuarioDuplicado(UsuarioDuplicadoException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> solicitudInvalida(IllegalArgumentException exception) {
        return Map.of("error", exception.getMessage());
    }
}