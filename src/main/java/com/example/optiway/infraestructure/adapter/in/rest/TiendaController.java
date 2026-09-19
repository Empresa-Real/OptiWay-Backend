package com.example.optiway.infraestructure.adapter.in.rest;

import com.example.optiway.application.port.in.CrearTiendaUseCase;
import com.example.optiway.domain.model.Tienda;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tiendas")
public class TiendaController {

    private final CrearTiendaUseCase crearTiendaUseCase;

    public TiendaController(CrearTiendaUseCase crearTiendaUseCase) {
        this.crearTiendaUseCase = crearTiendaUseCase;
    }

    @PostMapping
    public ResponseEntity<?> crearTienda(@RequestBody Tienda tienda) {
        try {
            Tienda nuevaTienda = crearTiendaUseCase.crearTienda(tienda);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaTienda);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}