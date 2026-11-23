package com.example.optiway.application.port.in.dto;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Reporte de rotación por tienda y periodo")
public record ReporteRotacionResponse(
        Long tiendaId,
        LocalDate desde,
        LocalDate hasta,
        String metodoCalculo,
        List<ReporteRotacionProductoResponse> productos) {
}
