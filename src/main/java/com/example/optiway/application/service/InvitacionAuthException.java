package com.example.optiway.application.service;

public class InvitacionAuthException extends RuntimeException {

    private final Integer statusCode;
    private final Object detalle;

    public InvitacionAuthException(String message) {
        super(message);
        this.statusCode = null;
        this.detalle = null;
    }

    public InvitacionAuthException(String message, Integer statusCode, Object detalle) {
        super(message);
        this.statusCode = statusCode;
        this.detalle = detalle;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public Object getDetalle() {
        return detalle;
    }
}