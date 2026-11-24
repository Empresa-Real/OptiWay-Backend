package com.example.optiway.application.port.in;

import com.example.optiway.application.port.in.dto.PatronesDemandaResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ConsultarPatronesDemandaUseCase {

    PatronesDemandaResponse consultarPatronesDemanda(
            UUID authUserId,
            Long productoId,
            List<Long> tiendaIds,
            LocalDate desde,
            LocalDate hasta
    );
}