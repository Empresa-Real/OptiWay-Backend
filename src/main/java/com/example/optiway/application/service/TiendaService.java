package com.example.optiway.application.service;

import com.example.optiway.application.port.in.CrearTiendaUseCase;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.domain.model.Tienda;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TiendaService implements CrearTiendaUseCase {

    private final TiendaRepositoryPort tiendaRepositoryPort;

    public TiendaService(TiendaRepositoryPort tiendaRepositoryPort) {
        this.tiendaRepositoryPort = tiendaRepositoryPort;
    }

    @Override
    public Tienda crearTienda(Tienda tienda) {
        // Validaciones de Criterios de Aceptación (HU-07)
        if (tienda.getNombre() == null || tienda.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la tienda es obligatorio.");
        }
        if (tienda.getDireccion() == null || tienda.getDireccion().trim().isEmpty()) {
            throw new IllegalArgumentException("La dirección es obligatoria.");
        }
        if (tienda.getCiudad() == null || tienda.getCiudad().trim().isEmpty()) {
            throw new IllegalArgumentException("La ciudad es obligatoria.");
        }

        // Asignación de Código Único y Estado
        tienda.setCodigo("TND-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        tienda.setEstado("Activa");

        return tiendaRepositoryPort.save(tienda);
    }
}