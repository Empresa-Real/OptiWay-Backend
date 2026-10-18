package com.example.optiway.infrastructure.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import com.example.optiway.infrastructure.adapter.in.rest.dto.AsignarEncargadoRequest;
import com.example.optiway.infrastructure.security.AuthUserId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.optiway.application.port.in.AsignarEncargadoCDUseCase;
import com.example.optiway.application.port.in.CrearCentroDistribucionUseCase;
import com.example.optiway.application.port.in.ListarCentrosDistribucionUseCase;
import com.example.optiway.application.port.in.RegistrarIngresoCommand;
import com.example.optiway.application.port.in.RegistrarIngresoMercanciaUseCase;
import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.domain.model.IngresoMercancia;
import com.example.optiway.infrastructure.adapter.in.rest.dto.CrearCentroDistribucionRequest;
import com.example.optiway.infrastructure.adapter.in.rest.dto.IngresoMercanciaResponse;
import com.example.optiway.infrastructure.adapter.in.rest.dto.RegistrarIngresoRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping({"/api/centros-distribucion", "/api/cd"})
public class CentroDistribucionController {

    private final CrearCentroDistribucionUseCase crearCentroDistribucionUseCase;
    private final ListarCentrosDistribucionUseCase listarCentrosDistribucionUseCase;
    private final AsignarEncargadoCDUseCase asignarEncargadoCDUseCase;
    private final RegistrarIngresoMercanciaUseCase registrarIngresoMercanciaUseCase;

    public CentroDistribucionController(
            CrearCentroDistribucionUseCase crearCentroDistribucionUseCase,
            ListarCentrosDistribucionUseCase listarCentrosDistribucionUseCase,
            AsignarEncargadoCDUseCase asignarEncargadoCDUseCase,
            RegistrarIngresoMercanciaUseCase registrarIngresoMercanciaUseCase) {
        this.crearCentroDistribucionUseCase = crearCentroDistribucionUseCase;
        this.listarCentrosDistribucionUseCase = listarCentrosDistribucionUseCase;
        this.asignarEncargadoCDUseCase = asignarEncargadoCDUseCase;
        this.registrarIngresoMercanciaUseCase = registrarIngresoMercanciaUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CentroDistribucion crear(
            @RequestBody CrearCentroDistribucionRequest request,
            @AuthUserId UUID authUserId) {
        CentroDistribucion domain = new CentroDistribucion(
                null,
                null,
                request.getNombre(),
                request.getDireccion(),
                request.getCapacidad(),
                request.getEncargadoId(),
                request.getTiendasAbastecidasIds()
        );
        return crearCentroDistribucionUseCase.crearCentroDistribucion(domain, authUserId);
    }

    @GetMapping
    public List<CentroDistribucion> listar(@AuthUserId UUID authUserId) {
        return listarCentrosDistribucionUseCase.listarCentrosDistribucion(authUserId);
    }

    @PutMapping("/{id}/encargado")
    public CentroDistribucion asignarEncargado(
            @PathVariable Long id,
            @RequestBody(required = false) AsignarEncargadoRequest request,
            @AuthUserId UUID authUserId) {
        Long encargadoId = request != null ? request.encargadoId() : null;
        return asignarEncargadoCDUseCase.asignarEncargado(id, encargadoId, authUserId);
    }

    @PostMapping("/{id}/ingresos")
    @ResponseStatus(HttpStatus.CREATED)
    public IngresoMercanciaResponse registrarIngreso(
            @PathVariable("id") Long centroDistribucionId,
            @Valid @RequestBody RegistrarIngresoRequest request,
            @AuthUserId UUID authUserId) {

        RegistrarIngresoCommand command = new RegistrarIngresoCommand(
                centroDistribucionId,
                request.getProductoId(),
                request.getCantidad(),
                request.getOrigen()
        );

        IngresoMercancia ingreso = registrarIngresoMercanciaUseCase.registrarIngreso(command, authUserId);
        return IngresoMercanciaResponse.from(ingreso);
    }
}
