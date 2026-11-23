package com.example.optiway.application.port.in.dto;

import java.time.LocalDate;
import java.util.List;

public record PatronesDemandaResponse(
        Long productoId,
        LocalDate desde,
        LocalDate hasta,
        List<DemandaTienda> tiendas,
        List<Long> tiendasMayorRotacion,
        List<Long> tiendasMenorRotacion
) {
    public record DemandaTienda(
            Long tiendaId,
            long unidadesVendidas,
            List<VentaDiaria> evolucion
    ) {}

    public record VentaDiaria(
            LocalDate fecha,
            long unidadesVendidas
    ) {}
}