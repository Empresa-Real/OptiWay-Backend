package com.example.optiway.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

import com.example.optiway.application.port.in.dto.ReporteRotacionResponse;

public interface ReporteRotacionUseCase {
    ReporteRotacionResponse generarReporte(UUID authUserId, Long tiendaId, LocalDate desde, LocalDate hasta);
}
