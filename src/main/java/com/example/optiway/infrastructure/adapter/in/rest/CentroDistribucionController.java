package com.example.optiway.infrastructure.adapter.in.rest;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ResponseEntity<CentroDistribucion> crear(
            @RequestBody CrearCentroDistribucionRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        java.util.UUID authUserId = jwt != null ? java.util.UUID.fromString(jwt.getSubject()) : null;
        CentroDistribucion domain = new CentroDistribucion(
                null,
                null,
                request.getNombre(),
                request.getDireccion(),
                request.getCapacidad(),
                request.getEncargadoId(),
                request.getTiendasAbastecidasIds()
        );
        CentroDistribucion creado = crearCentroDistribucionUseCase.crearCentroDistribucion(domain, authUserId);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CentroDistribucion>> listar(
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        java.util.UUID authUserId = jwt != null ? java.util.UUID.fromString(jwt.getSubject()) : null;
        return ResponseEntity.ok(crearCentroDistribucionUseCase.listarCentrosDistribucion(authUserId));
    }

    @PutMapping("/{id}/encargado")
    public ResponseEntity<?> asignarEncargado(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        try {
            java.util.UUID authUserId = jwt != null ? java.util.UUID.fromString(jwt.getSubject()) : null;
            Long encargadoId = null;
            if (body != null && body.get("encargadoId") != null) {
                encargadoId = Long.valueOf(body.get("encargadoId").toString());
            }
            CentroDistribucion actualizado = asignarEncargadoCDUseCase.asignarEncargado(id, encargadoId, authUserId);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
