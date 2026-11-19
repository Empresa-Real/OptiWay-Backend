package com.example.optiway.application.port.in;

import java.util.UUID;
import com.example.optiway.application.port.in.dto.RecomendacionReposicionResponse;

public interface CrearRecomendacionReposicionUseCase {

    RecomendacionReposicionResponse crear(
            UUID authUserId,
            CrearRecomendacionReposicionCommand command
    );
}