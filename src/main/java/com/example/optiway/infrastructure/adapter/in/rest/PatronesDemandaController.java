package com.example.optiway.infrastructure.adapter.in.rest;

import com.example.optiway.application.port.in.ConsultarPatronesDemandaUseCase;
import com.example.optiway.application.port.in.dto.PatronesDemandaResponse;
import com.example.optiway.infrastructure.adapter.in.rest.docs.ApiBadRequestResponse;
import com.example.optiway.infrastructure.adapter.in.rest.docs.ApiCommonErrorResponses;
import com.example.optiway.infrastructure.adapter.in.rest.docs.ApiResourceNotFoundResponse;
import com.example.optiway.infrastructure.security.AuthUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/demanda")
@Tag(name = "Demanda", description = "Histórico de ventas por producto y tienda")
public class PatronesDemandaController {

    private final ConsultarPatronesDemandaUseCase consultarPatronesDemandaUseCase;

    public PatronesDemandaController(
            ConsultarPatronesDemandaUseCase consultarPatronesDemandaUseCase) {
        this.consultarPatronesDemandaUseCase = consultarPatronesDemandaUseCase;
    }

    @GetMapping("/patrones")
    @ApiBadRequestResponse
    @ApiResourceNotFoundResponse
    @ApiCommonErrorResponses
    @Operation(summary = "Consultar patrones de demanda",
            description = "Devuelve las unidades vendidas por día y por tienda, "
                    + "e identifica las tiendas con mayor y menor total en el período. "
                    + "Máximo 366 días y 50 tiendas por consulta. Requiere rol PLANIFICADOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Patrones de demanda calculados correctamente")
    })
    public PatronesDemandaResponse consultarPatrones(
            @Parameter(hidden = true) @AuthUserId UUID authUserId,
            @RequestParam("productoId") Long productoId,
            @RequestParam("tiendaIds") List<Long> tiendaIds,
            @RequestParam("desde")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam("hasta")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        return consultarPatronesDemandaUseCase.consultarPatronesDemanda(
                authUserId, productoId, tiendaIds, desde, hasta);
    }
}