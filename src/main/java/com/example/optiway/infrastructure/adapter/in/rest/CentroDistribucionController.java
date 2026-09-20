package com.example.optiway.infrastructure.adapter.in.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.optiway.application.port.in.CrearCentroDistribucionUseCase;
import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.infrastructure.adapter.in.rest.dto.CrearCentroDistribucionRequest;

@RestController
@RequestMapping("/api/centros-distribucion")
public class CentroDistribucionController {

    private final CrearCentroDistribucionUseCase crearCentroDistribucionUseCase;

    public CentroDistribucionController(CrearCentroDistribucionUseCase crearCentroDistribucionUseCase) {
        this.crearCentroDistribucionUseCase = crearCentroDistribucionUseCase;
    }

    @PostMapping
    public ResponseEntity<CentroDistribucion> crear(@RequestBody CrearCentroDistribucionRequest request) {
        CentroDistribucion domain = new CentroDistribucion(
                null,
                null,
                request.getNombre(),
                request.getDireccion(),
                request.getCapacidad(),
                request.getTiendasAbastecidasIds()
        );
        CentroDistribucion creado = crearCentroDistribucionUseCase.crearCentroDistribucion(domain);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CentroDistribucion>> listar() {
        return ResponseEntity.ok(crearCentroDistribucionUseCase.listarCentrosDistribucion());
    }
}
