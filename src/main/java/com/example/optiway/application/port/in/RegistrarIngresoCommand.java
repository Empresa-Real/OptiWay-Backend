package com.example.optiway.application.port.in;

/**
 * Comando inmutable que encapsula los datos requeridos para registrar un ingreso
 * de mercancía a un Centro de Distribución.
 */
public record RegistrarIngresoCommand(
        Long centroDistribucionId,
        Long productoId,
        Integer cantidad,
        String origen
) {
    public RegistrarIngresoCommand {
        if (origen != null) {
            origen = origen.trim();
        }
    }
}
