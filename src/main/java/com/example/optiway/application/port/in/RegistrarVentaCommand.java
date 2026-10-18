package com.example.optiway.application.port.in;

import java.time.LocalDateTime;

public record RegistrarVentaCommand(
        Long tiendaId,
        Long productoId,
        Integer cantidad,
        LocalDateTime fechaHora
) {}
