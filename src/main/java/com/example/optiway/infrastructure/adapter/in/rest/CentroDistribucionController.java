package com.example.optiway.infrastructure.adapter.in.rest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.infrastructure.adapter.in.rest.dto.CrearCentroDistribucionRequest;

@RestController
@RequestMapping("/api/centros-distribucion")
public class CentroDistribucionController {

    private final CrearCentroDistribucionUseCase crearCentroDistribucionUseCase;
    private final AsignarEncargadoCDUseCase asignarEncargadoCDUseCase;

    public CentroDistribucionController(
            CrearCentroDistribucionUseCase crearCentroDistribucionUseCase,
            AsignarEncargadoCDUseCase asignarEncargadoCDUseCase) {
        this.crearCentroDistribucionUseCase = crearCentroDistribucionUseCase;
        this.asignarEncargadoCDUseCase = asignarEncargadoCDUseCase;
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
        return crearCentroDistribucionUseCase.listarCentrosDistribucion(authUserId);
    }

    @PutMapping("/{id}/encargado")
    public CentroDistribucion asignarEncargado(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body,
            @AuthUserId UUID authUserId) {
        Long encargadoId = null;
        if (body != null && body.get("encargadoId") != null) {
            encargadoId = Long.valueOf(body.get("encargadoId").toString());
        }
        return asignarEncargadoCDUseCase.asignarEncargado(id, encargadoId, authUserId);
    }
}
