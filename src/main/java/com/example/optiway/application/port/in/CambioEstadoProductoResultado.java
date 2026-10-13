
package com.example.optiway.application.port.in;

public class CambioEstadoProductoResultado {

    private final boolean activo;
    private final boolean requiereConfirmacion;
    private final boolean tieneExistencias;
    private final String mensaje;

    public CambioEstadoProductoResultado(
            boolean activo,
            boolean requiereConfirmacion,
            boolean tieneExistencias,
            String mensaje) {
        this.activo = activo;
        this.requiereConfirmacion = requiereConfirmacion;
        this.tieneExistencias = tieneExistencias;
        this.mensaje = mensaje;
    }

    public boolean isActivo() {
        return activo;
    }

    public boolean isRequiereConfirmacion() {
        return requiereConfirmacion;
    }

    public boolean isTieneExistencias() {
        return tieneExistencias;
    }

    public String getMensaje() {
        return mensaje;
    }
}
