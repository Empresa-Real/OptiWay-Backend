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

import com.example.optiway.application.port.in.AsignarEncargadoTiendaUseCase;
import com.example.optiway.application.port.in.CrearTiendaUseCase;
import com.example.optiway.application.port.in.ObtenerTiendasUseCase;
import com.example.optiway.domain.model.Tienda;

@RestController
@RequestMapping("/api/tiendas")
public class TiendaController {

    private final CrearTiendaUseCase crearTiendaUseCase;
    private final ObtenerTiendasUseCase obtenerTiendasUseCase;
    private final AsignarEncargadoTiendaUseCase asignarEncargadoTiendaUseCase;

    public TiendaController(
            CrearTiendaUseCase crearTiendaUseCase,
            ObtenerTiendasUseCase obtenerTiendasUseCase,
            AsignarEncargadoTiendaUseCase asignarEncargadoTiendaUseCase) {
        this.crearTiendaUseCase = crearTiendaUseCase;
        this.obtenerTiendasUseCase = obtenerTiendasUseCase;
        this.asignarEncargadoTiendaUseCase = asignarEncargadoTiendaUseCase;
    }

    @PostMapping
    public ResponseEntity<?> crearTienda(
            @RequestBody Tienda tienda,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        try {
            java.util.UUID authUserId = jwt != null ? java.util.UUID.fromString(jwt.getSubject()) : null;
            Tienda nuevaTienda = crearTiendaUseCase.crearTienda(tienda, authUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaTienda);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Tienda>> obtenerTiendas(
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        java.util.UUID authUserId = jwt != null ? java.util.UUID.fromString(jwt.getSubject()) : null;
        return ResponseEntity.ok(obtenerTiendasUseCase.obtenerTiendas(authUserId));
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
            Tienda actualizada = asignarEncargadoTiendaUseCase.asignarEncargado(id, encargadoId, authUserId);
            return ResponseEntity.ok(actualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}