package com.example.optiway;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.optiway.application.port.in.CambiarEstadoProductoCommand;
import com.example.optiway.application.port.out.AuthorizationPort;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.application.port.in.dto.ProductoResponse;
import com.example.optiway.application.service.ProductoService;
import com.example.optiway.domain.model.Producto;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepositoryPort productoRepositoryPort;

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    @Mock
    private InventarioRepositoryPort inventarioRepositoryPort;

    @Mock
    private AuthorizationPort authorizationService;

    private ProductoService service;

    private final UUID authUserId = UUID.randomUUID();
    private final Long productoId = 10L;

    private Producto producto;

    @BeforeEach
    void setUp() {
        service = new ProductoService(
                productoRepositoryPort,
                usuarioRepositoryPort,
                inventarioRepositoryPort,
                authorizationService
        );

        producto = new Producto(
                productoId,
                "PROD-10",
                "Camisa",
                "Ropa",
                new BigDecimal("50000"),
                "Unidad",
                true
        );
    }

    private void prepararProducto() {
        when(productoRepositoryPort.findById(productoId))
                .thenReturn(Optional.of(producto));
    }

    @Test
    void desactivarProducto_SinExistencias_DebeDesactivarlo() {
        prepararProducto();

        when(inventarioRepositoryPort.existeStockActivoDeProducto(productoId))
                .thenReturn(false);
        when(productoRepositoryPort.save(any(Producto.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ProductoResponse respuesta = service.cambiarEstadoProducto(
                authUserId,
                new CambiarEstadoProductoCommand(productoId, false, false)
        );

        assertFalse(respuesta.activo());
        assertEquals("inactivo", respuesta.estado());
        assertFalse(producto.isActivo());

        verify(productoRepositoryPort).save(producto);
    }

    @Test
    void desactivarProducto_ConExistenciasYSinConfirmacion_DebeRechazarLaOperacion() {
        prepararProducto();

        when(inventarioRepositoryPort.existeStockActivoDeProducto(productoId))
                .thenReturn(true);

        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> service.cambiarEstadoProducto(
                        authUserId,
                        new CambiarEstadoProductoCommand(productoId, false, false)
                )
        );

        assertEquals(HttpStatus.CONFLICT, excepcion.getStatusCode());
        assertTrue(excepcion.getReason().contains("existencias activas"));

        assertTrue(producto.isActivo());
        verify(productoRepositoryPort, never()).save(any(Producto.class));
    }

    @Test
    void desactivarProducto_ConExistenciasYConfirmacion_DebeDesactivarlo() {
        prepararProducto();

        when(inventarioRepositoryPort.existeStockActivoDeProducto(productoId))
                .thenReturn(true);
        when(productoRepositoryPort.save(any(Producto.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ProductoResponse respuesta = service.cambiarEstadoProducto(
                authUserId,
                new CambiarEstadoProductoCommand(productoId, false, true)
        );

        assertFalse(respuesta.activo());
        assertEquals("inactivo", respuesta.estado());
        assertFalse(producto.isActivo());

        verify(productoRepositoryPort).save(producto);
    }

    @Test
    void reactivarProducto_NoDebeExigirConfirmacionDeExistencias() {
        producto.setActivo(false);
        prepararProducto();

        when(productoRepositoryPort.save(any(Producto.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ProductoResponse respuesta = service.cambiarEstadoProducto(
                authUserId,
                new CambiarEstadoProductoCommand(productoId, true, false)
        );

        assertTrue(respuesta.activo());
        assertEquals("activo", respuesta.estado());
        assertTrue(producto.isActivo());

        verifyNoInteractions(inventarioRepositoryPort);
        verify(productoRepositoryPort).save(producto);
    }
}
