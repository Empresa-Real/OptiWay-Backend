package com.example.optiway.application.port.in;

import java.util.UUID;

import com.example.optiway.application.port.in.dto.OrdenReposicionResponse;

public interface AprobarRecomendacionUseCase {

    OrdenReposicionResponse aprobar(UUID authUserId, AprobarRecomendacionCommand command);
}
