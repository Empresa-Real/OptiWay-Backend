package com.example.optiway.infrastructure.adapter.in.rest.dto;

public class CambiarEstadoProductoRequest {

    private Boolean activo;

    public CambiarEstadoProductoRequest() {
    }

    public CambiarEstadoProductoRequest(Boolean activo) {
        this.activo = activo;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
