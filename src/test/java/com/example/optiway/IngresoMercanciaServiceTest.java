package com.example.optiway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import com.example.optiway.application.port.in.RegistrarIngresoCommand;
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
import com.example.optiway.infrastructure.security.AuthorizationService;

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
        AuthorizationService authorizationService = new AuthorizationService(usuarioRepositoryPort);
        service = new IngresoMercanciaService(
                ingresoRepositoryPort,
                inventarioRepositoryPort,
                centroDistribucionRepositoryPort,
                productoRepositoryPort,
                authorizationService);
    }

    @Test
    void registrarIngreso_CuandoEsValido_DebeGuardarIngresoYSumarInventario() {
        UUID authUserId = UUID.randomUUID();
        Long centroId = 1L;
        Long productoId = 10L;
        Integer cantidad = 50;
        String origen = "Proveedor Nacional";

        Usuario usuario = new Usuario(1L, null, "Encargado", "user@test.com", Rol.ENCARGADO_CD, null);
        CentroDistribucion centro = new CentroDistribucion(centroId, "CD-01", "Principal", "Dir", 1000, 1L, null);
        Producto producto = new Producto(productoId, null, "Arroz", "Granos", null, null, true);

        Inventario inventarioExistente = new Inventario();
        inventarioExistente.setCentroDistribucionId(centroId);
        inventarioExistente.setProductoId(productoId);
        inventarioExistente.setCantidadActual(30);

        when(usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)).thenReturn(Optional.of(usuario));
        when(centroDistribucionRepositoryPort.obtenerPorId(centroId)).thenReturn(Optional.of(centro));
        when(productoRepositoryPort.findById(productoId)).thenReturn(Optional.of(producto));
        when(inventarioRepositoryPort.findByCentroDistribucionIdAndProductoId(centroId, productoId))
                .thenReturn(Optional.of(inventarioExistente));

        when(ingresoRepositoryPort.guardar(any(IngresoMercancia.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        IngresoMercancia resultado = service.registrarIngreso(
                new RegistrarIngresoCommand(centroId, productoId, cantidad, origen), authUserId);

        ArgumentCaptor<Inventario> invCaptor = ArgumentCaptor.forClass(Inventario.class);
        verify(inventarioRepositoryPort).save(invCaptor.capture());
        assertEquals(80, invCaptor.getValue().getCantidadActual());

        ArgumentCaptor<IngresoMercancia> ingCaptor = ArgumentCaptor.forClass(IngresoMercancia.class);
        verify(ingresoRepositoryPort).guardar(ingCaptor.capture());
        assertEquals(cantidad, ingCaptor.getValue().getCantidad());
        assertEquals(origen, ingCaptor.getValue().getOrigen());

        assertNotNull(resultado);
        assertEquals(cantidad, resultado.getCantidad());
    }

    @Test
    void registrarIngreso_CuandoInventarioEsNuevo_DebeInicializarInventario() {
        UUID authUserId = UUID.randomUUID();
        Long centroId = 1L;
        Long productoId = 10L;
        Integer cantidad = 25;
        String origen = "Proveedor Internacional";

        Usuario usuario = new Usuario(1L, null, "Encargado", "user@test.com", Rol.ENCARGADO_CD, null);
        CentroDistribucion centro = new CentroDistribucion(centroId, "CD-01", "Principal", "Dir", 1000, 1L, null);
        Producto producto = new Producto(productoId, null, "Harina", "Abarrotes", null, null, true);

        when(usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)).thenReturn(Optional.of(usuario));
        when(centroDistribucionRepositoryPort.obtenerPorId(centroId)).thenReturn(Optional.of(centro));
        when(productoRepositoryPort.findById(productoId)).thenReturn(Optional.of(producto));
        when(inventarioRepositoryPort.findByCentroDistribucionIdAndProductoId(centroId, productoId))
                .thenReturn(Optional.empty());
        when(ingresoRepositoryPort.guardar(any(IngresoMercancia.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        IngresoMercancia resultado = service.registrarIngreso(
                new RegistrarIngresoCommand(centroId, productoId, cantidad, origen), authUserId);

        ArgumentCaptor<Inventario> invCaptor = ArgumentCaptor.forClass(Inventario.class);
        verify(inventarioRepositoryPort).save(invCaptor.capture());
        assertEquals(25, invCaptor.getValue().getCantidadActual());
        assertEquals(centroId, invCaptor.getValue().getCentroDistribucionId());
        assertEquals(productoId, invCaptor.getValue().getProductoId());

        assertNotNull(resultado);
        assertEquals(cantidad, resultado.getCantidad());
    }

    @Test
    void registrarIngreso_CuandoExcedeCapacidadCD_DebeLanzarExcepcion() {
        UUID authUserId = UUID.randomUUID();
        Long centroId = 1L;
        Long productoId = 10L;

        Usuario usuario = new Usuario(1L, null, "Encargado", "user@test.com", Rol.ENCARGADO_CD, null);
        CentroDistribucion centro = new CentroDistribucion(centroId, "CD-01", "Principal", "Dir", 100, 1L, null);
        Producto producto = new Producto(productoId, null, "Arroz", "Granos", null, null, true);

        Inventario inventarioExistente = new Inventario();
        inventarioExistente.setCentroDistribucionId(centroId);
        inventarioExistente.setProductoId(productoId);
        inventarioExistente.setCantidadActual(90);

        when(usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)).thenReturn(Optional.of(usuario));
        when(centroDistribucionRepositoryPort.obtenerPorId(centroId)).thenReturn(Optional.of(centro));
        when(productoRepositoryPort.findById(productoId)).thenReturn(Optional.of(producto));
        when(inventarioRepositoryPort.findByCentroDistribucionId(centroId))
                .thenReturn(java.util.List.of(inventarioExistente));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarIngreso(
                        new RegistrarIngresoCommand(centroId, productoId, 20, "Proveedor"),
                        authUserId
                )
        );

        org.junit.jupiter.api.Assertions.assertTrue(ex.getMessage().contains("excede la capacidad máxima"));
        verify(inventarioRepositoryPort, never()).save(any());
        verify(ingresoRepositoryPort, never()).guardar(any());
    }

    @Test
    void registrarIngreso_CuandoCDNoExiste_DebeLanzarExcepcion() {
        UUID authUserId = UUID.randomUUID();
        when(centroDistribucionRepositoryPort.obtenerPorId(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarIngreso(
                        new RegistrarIngresoCommand(99L, 1L, 10, "Proveedor"),
                        authUserId
                )
        );

        assertEquals("Centro de distribución no encontrado", ex.getMessage());
        verifyNoInteractions(ingresoRepositoryPort, inventarioRepositoryPort);
    }

    @Test
    void registrarIngreso_CuandoProductoNoExiste_DebeLanzarExcepcion() {
        UUID authUserId = UUID.randomUUID();
        Long centroId = 1L;

        Usuario usuario = new Usuario(1L, null, "Encargado", "user@test.com", Rol.ENCARGADO_CD, null);
        CentroDistribucion centro = new CentroDistribucion(centroId, "CD-01", "Principal", "Dir", 1000, 1L, null);

        when(centroDistribucionRepositoryPort.obtenerPorId(centroId)).thenReturn(Optional.of(centro));
        when(usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)).thenReturn(Optional.of(usuario));
        when(productoRepositoryPort.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarIngreso(
                        new RegistrarIngresoCommand(centroId, 99L, 10, "Proveedor"),
                        authUserId
                )
        );

        assertEquals("Producto no encontrado", ex.getMessage());
        verifyNoInteractions(ingresoRepositoryPort, inventarioRepositoryPort);
    }

    @Test
    void debeRechazarCantidadCero() {
        UUID authUserId = UUID.randomUUID();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarIngreso(
                        new RegistrarIngresoCommand(1L, 1L, 0, "Proveedor"),
                        authUserId
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
                        new RegistrarIngresoCommand(1L, 1L, -10, "Proveedor"),
                        authUserId
                )
        );

        verifyNoInteractions(ingresoRepositoryPort, inventarioRepositoryPort);
    }
}