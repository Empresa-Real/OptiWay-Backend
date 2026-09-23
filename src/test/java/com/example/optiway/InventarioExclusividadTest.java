package com.example.optiway;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.example.optiway.domain.model.Inventario;

class InventarioExclusividadTest {

    @Test
    void inventario_ConSoloTienda_EsValido() {
        assertDoesNotThrow(() -> {
            Inventario inv = new Inventario(1L, 10L, null, 5L, 100, 10);
            inv.validarUbicacionExclusiva();
        });
    }

    @Test
    void inventario_ConSoloCentroDistribucion_EsValido() {
        assertDoesNotThrow(() -> {
            Inventario inv = new Inventario(1L, null, 20L, 5L, 100, 10);
            inv.validarUbicacionExclusiva();
        });
    }

    @Test
    void inventario_ConTiendaYCentroDistribucionSimultaneos_DebeLanzarError() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Inventario(1L, 10L, 20L, 5L, 100, 10);
        });
    }

    @Test
    void inventario_SinNingunaUbicacion_DebeLanzarError() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Inventario(1L, null, null, 5L, 100, 10);
        });
    }
}
