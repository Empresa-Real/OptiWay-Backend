package com.example.optiway.application.port.in;

import com.example.optiway.application.port.in.dto.AjusteInventarioResponse;
import java.util.UUID;

public interface AjustarInventarioUseCase {
    AjusteInventarioResponse ajustarInventario(UUID authUserId, AjustarInventarioCommand command);
}
