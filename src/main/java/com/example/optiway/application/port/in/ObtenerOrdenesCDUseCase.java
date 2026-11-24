package com.example.optiway.application.port.in;

import java.util.List;
import java.util.UUID;

import com.example.optiway.application.port.in.dto.OrdenReposicionResponse;

public interface ObtenerOrdenesCDUseCase {

    List<OrdenReposicionResponse> obtenerOrdenesPorCentro(UUID authUserId, Long centroDistribucionId);
}
