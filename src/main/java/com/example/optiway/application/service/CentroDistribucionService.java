package com.example.optiway.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.optiway.application.port.in.CrearCentroDistribucionUseCase;
import com.example.optiway.application.port.out.CentroDistribucionRepositoryPort;
import com.example.optiway.domain.model.CentroDistribucion;

@Service
public class CentroDistribucionService implements CrearCentroDistribucionUseCase {

    private final CentroDistribucionRepositoryPort centroDistribucionRepositoryPort;

    public CentroDistribucionService(CentroDistribucionRepositoryPort centroDistribucionRepositoryPort) {
        this.centroDistribucionRepositoryPort = centroDistribucionRepositoryPort;
    }

    @Override
    public CentroDistribucion crearCentroDistribucion(CentroDistribucion centroDistribucion) {
        if (centroDistribucion.getDireccion() == null || centroDistribucion.getDireccion().trim().isEmpty()) {
            throw new IllegalArgumentException("La dirección del centro de distribución es obligatoria.");
        }

        if (centroDistribucion.getNombre() == null || centroDistribucion.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del centro de distribución es obligatorio.");
        }

        String codigoUnico = "CD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        centroDistribucion.setCodigo(codigoUnico);

        return centroDistribucionRepositoryPort.guardar(centroDistribucion);
    }

    @Override
    public List<CentroDistribucion> listarCentrosDistribucion() {
        return centroDistribucionRepositoryPort.obtenerTodos();
    }
}