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
    @ResponseStatus(HttpStatus.CREATED)
    public Tienda crearTienda(
            @RequestBody Tienda tienda,
            @AuthUserId UUID authUserId) {
        return crearTiendaUseCase.crearTienda(tienda, authUserId);
    }

    @GetMapping
    public List<Tienda> obtenerTiendas(@AuthUserId UUID authUserId) {
        return obtenerTiendasUseCase.obtenerTiendas(authUserId);
    }

    @PutMapping("/{id}/encargado")
    public Tienda asignarEncargado(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body,
            @AuthUserId UUID authUserId) {
        Long encargadoId = null;
        if (body != null && body.get("encargadoId") != null) {
            encargadoId = Long.valueOf(body.get("encargadoId").toString());
        }
        return asignarEncargadoTiendaUseCase.asignarEncargado(id, encargadoId, authUserId);
    }
}