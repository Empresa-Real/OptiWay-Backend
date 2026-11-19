package com.example.optiway.application.port.in.dto;

import java.time.LocalDateTime;

public record SolicitudReposicionResponse(
        Long id,
        Long tiendaId,
        Long productoId,
        String nombreProducto,
        Integer cantidad,
        String estado,
        Long usuarioId,
        LocalDateTime fechaSolicitud
) {}