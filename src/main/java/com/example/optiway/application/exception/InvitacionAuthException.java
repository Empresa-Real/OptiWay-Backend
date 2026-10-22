package com.example.optiway.application.exception;

public class InvitacionAuthException extends RuntimeException {

    private final Integer statusCode;
    private final Object detalle;

    public InvitacionAuthException(String message, Integer statusCode, Object detalle) {
        super(message);
        this.statusCode = statusCode;
        this.detalle = detalle;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getError() {
        return detalle != null ? detalle.toString() : getMessage();
    }
}
