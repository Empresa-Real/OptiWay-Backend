package com.example.optiway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.optiway.application.port.out.CentroDistribucionRepositoryPort;
import com.example.optiway.application.port.out.IngresoMercanciaRepositoryPort;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.application.service.IngresoMercanciaService;
import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.domain.model.IngresoMercancia;
import com.example.optiway.domain.model.Inventario;
import com.example.optiway.domain.model.Producto;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.Usuario;

@ExtendWith(MockitoExtension.class)
class IngresoMercanciaServiceTest {

    @Mock
    private IngresoMercanciaRepositoryPort ingresoRepositoryPort;

    @Mock
    private InventarioRepositoryPort inventarioRepositoryPort;

    @Mock
    private CentroDistribucionRepositoryPort centroDistribucionRepositoryPort;

    @Mock
    private ProductoRepositoryPort productoRepositoryPort;

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    private IngresoMercanciaService service;

    @BeforeEach
    void setUp() {
        service = new IngresoMercanciaService(
                ingresoRepositoryPort,
                inventarioRepositoryPort,
                centroDistribucionRepositoryPort,
                productoRepositoryPort,
                usuarioRepositoryPort);
    }

    @Test
    void registrarIngreso_CuandoEsValido_DebeGuardarIngresoYSumarInventario() {
        UUID authUserId = UUID.randomUUID();
        Long centroId = 1L;
        Long productoId = 10L;
        Integer cantidad = 50;
        String origen = "Proveedor Nacional";

        Usuario usuario = new Usuario(1L, "Encargado", "user@test.com", Rol.ENCARGADO_CD, null);
        CentroDistribucion centro = new CentroDistribucion(centroId, "CD-01", "Principal", "Dir", 1000, 1L, null);
        Producto producto = new Producto(productoId, "Arroz", "Granos");

        Inventario inventarioExistente = new Inventario();
        inventarioExistente.setCentroDistribucionId(centroId);
        inventarioExistente.setProductoId(productoId);
        inventarioExistente.setCantidadActual(30);

        when(usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)).thenReturn(Optional.of(usuario));
        when(centroDistribucionRepositoryPort.obtenerPorId(centroId)).thenReturn(Optional.of(centro));
        when(productoRepositoryPort.findById(productoId)).thenReturn(Optional.of(producto));
        when(inventarioRepositoryPort.findByCentroDistribucionIdAndProductoId(centroId, productoId))
                .thenReturn(Optional.of(inventarioExistente));

        service.registrarIngreso(authUserId, centroId, productoId, cantidad, origen);

        ArgumentCaptor<Inventario> invCaptor = ArgumentCaptor.forClass(Inventario.class);
        verify(inventarioRepositoryPort).save(invCaptor.capture());
        assertEquals(80, invCaptor.getValue().getCantidadActual());

        ArgumentCaptor<IngresoMercancia> ingCaptor = ArgumentCaptor.forClass(IngresoMercancia.class);
        verify(ingresoRepositoryPort).guardar(ingCaptor.capture());
        assertEquals(cantidad, ingCaptor.getValue().getCantidad());
        assertEquals(origen, ingCaptor.getValue().getOrigen());
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

        verifyNoInteractions(ingresoRepositoryPort, inventarioRepositoryPort);
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

        verifyNoInteractions(ingresoRepositoryPort, inventarioRepositoryPort);
    }
}