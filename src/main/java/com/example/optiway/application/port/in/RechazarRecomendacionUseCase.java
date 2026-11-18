package com.example.optiway.application.port.in;

import java.util.UUID;

import com.example.optiway.application.port.in.dto.RecomendacionReposicionResponse;

public interface RechazarRecomendacionUseCase {

    RecomendacionReposicionResponse rechazar(UUID authUserId, RechazarRecomendacionCommand command);
}
