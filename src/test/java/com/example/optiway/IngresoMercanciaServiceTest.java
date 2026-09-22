package com.example.optiway;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.optiway.application.port.out.CentroDistribucionRepositoryPort;
import com.example.optiway.application.port.out.IngresoMercanciaRepositoryPort;
import com.example.optiway.application.port.out.InventarioCentroDistribucionRepositoryPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.application.service.IngresoMercanciaService;

public class IngresoMercanciaServiceTest {

    @Mock
    private IngresoMercanciaRepositoryPort ingresoRepositoryPort;

    @Mock
    private InventarioCentroDistribucionRepositoryPort inventarioRepositoryPort;

    @Mock
    private CentroDistribucionRepositoryPort centroDistribucionRepositoryPort;

    @Mock
    private ProductoRepositoryPort productoRepositoryPort;

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    private IngresoMercanciaService service;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        service = new IngresoMercanciaService(
                ingresoRepositoryPort,
                inventarioRepositoryPort,
                centroDistribucionRepositoryPort,
                productoRepositoryPort,
                usuarioRepositoryPort
        );
    }

    @Test
    void debeRechazarCantidadCero() {

        UUID authUserId = UUID.randomUUID();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarIngreso(
                        authUserId,
                        1L,
                        1L,
                        0,
                        "Proveedor"
                )
        );

        verifyNoInteractions(
                ingresoRepositoryPort,
                inventarioRepositoryPort
        );
    }

    @Test
    void debeRechazarCantidadNegativa() {

        UUID authUserId = UUID.randomUUID();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarIngreso(
                        authUserId,
                        1L,
                        1L,
                        -10,
                        "Proveedor"
                )
        );

        verifyNoInteractions(
                ingresoRepositoryPort,
                inventarioRepositoryPort
        );
    }
    
}