package com.example.optiway.application.port.in;

import java.util.List;
import java.util.UUID;
import com.example.optiway.application.port.in.dto.RecomendacionReposicionResponse;

public interface ConsultarRecomendacionesReposicionUseCase {

    List<RecomendacionReposicionResponse> consultar(UUID authUserId);
}