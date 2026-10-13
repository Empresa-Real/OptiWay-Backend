
package com.example.optiway.infrastructure.adapter.in.rest;

public class CambiarEstadoProductoRequest {

    private Boolean activo;
    private Boolean confirmar = false;

    public CambiarEstadoProductoRequest() {
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Boolean getConfirmar() {
        return confirmar;
    }

    public void setConfirmar(Boolean confirmar) {
        this.confirmar = confirmar;
    }
}
