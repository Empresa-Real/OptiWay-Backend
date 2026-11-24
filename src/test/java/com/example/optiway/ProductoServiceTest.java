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

import com.example.optiway.application.exception.ProductoDuplicadoException;
import com.example.optiway.application.exception.RecursoNoEncontradoException;
import com.example.optiway.application.port.in.CambiarEstadoProductoCommand;
import com.example.optiway.application.port.in.CrearProductoCommand;
import com.example.optiway.application.port.in.EditarProductoCommand;
import com.example.optiway.application.port.out.AuthorizationPort;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.application.port.in.dto.ProductoResponse;
import com.example.optiway.application.service.ProductoService;
import com.example.optiway.domain.model.Producto;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.Usuario;
import com.example.optiway.infrastructure.security.AuthorizedAction;

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

    @Test
    void cambiarEstadoProducto_ProductoInexistente_DebeLanzar404() {
        when(productoRepositoryPort.findById(productoId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.cambiarEstadoProducto(
                        authUserId,
                        new CambiarEstadoProductoCommand(productoId, false, false)
                )
        );

        verify(productoRepositoryPort, never()).save(any(Producto.class));
    }

    @Test
    void editarProducto_ProductoInexistente_DebeLanzar404() {
        when(productoRepositoryPort.findById(productoId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.editarProducto(
                        authUserId,
                        new EditarProductoCommand(
                                productoId, "Camisa nueva", null, null, null
                        )
                )
        );

        verify(productoRepositoryPort, never()).update(any(Producto.class), any());
    }

    @Test
    void crearProducto_CuandoDatosSonValidos_DebeNormalizarIdentificadorYGuardarActivo() {
        when(productoRepositoryPort.existsByIdentificador("PRD-NEW")).thenReturn(false);
        when(productoRepositoryPort.save(any(Producto.class))).thenAnswer(inv -> {
            Producto p = inv.getArgument(0);
            p.setId(99L);
            return p;
        });

        ProductoResponse res = service.crearProducto(authUserId, new CrearProductoCommand(
                "  prd-new ", "  Arroz  ", "  Granos  ", new BigDecimal("4500"), "  kg  "));

        assertEquals(99L, res.id());
        assertEquals("PRD-NEW", res.identificador());
        assertEquals("Arroz", res.nombre());
        assertEquals("Granos", res.categoria());
        assertEquals("KG", res.unidadMedida());
        assertTrue(res.activo());
        verify(authorizationService).requireGestionLogistica(authUserId, AuthorizedAction.CREAR_PRODUCTO);
    }

    @Test
    void crearProducto_CuandoIdentificadorYaExiste_DebeLanzarProductoDuplicado() {
        when(productoRepositoryPort.existsByIdentificador("PRD-DUP")).thenReturn(true);

        assertThrows(ProductoDuplicadoException.class, () -> service.crearProducto(
                authUserId, new CrearProductoCommand("prd-dup", "Arroz", "Granos", new BigDecimal("4500"), "kg")));
        verify(productoRepositoryPort, never()).save(any());
    }

    @Test
    void crearProducto_CuandoPrecioEsNegativo_DebeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> service.crearProducto(
                authUserId, new CrearProductoCommand("prd-1", "Arroz", "Granos", new BigDecimal("-1"), "kg")));
        verify(productoRepositoryPort, never()).save(any());
    }

    @Test
    void editarProducto_CuandoDatosSonValidos_DebeActualizarCampos() {
        Usuario admin = new Usuario(1L, authUserId, "Admin", "admin@opt.com", Rol.ADMINISTRADOR, null);
        when(authorizationService.requireGestionLogistica(authUserId, AuthorizedAction.EDITAR_PRODUCTO))
                .thenReturn(admin);
        prepararProducto();
        when(productoRepositoryPort.update(any(Producto.class), eq(1L)))
                .thenAnswer(inv -> inv.getArgument(0));

        ProductoResponse res = service.editarProducto(authUserId, new EditarProductoCommand(
                productoId, "  Camisa Polo  ", null, new BigDecimal("60000"), null));

        assertEquals("Camisa Polo", res.nombre());
        assertEquals(new BigDecimal("60000"), res.precio());
        verify(productoRepositoryPort).update(producto, 1L);
    }

    @Test
    void editarProducto_CuandoNoSeEnviaNingunCampo_DebeLanzarExcepcion() {
        prepararProducto();

        assertThrows(IllegalArgumentException.class, () -> service.editarProducto(
                authUserId, new EditarProductoCommand(productoId, null, null, null, null)));
        verify(productoRepositoryPort, never()).update(any(), any());
    }

    @Test
    void editarProducto_CuandoPrecioEsCeroONegativo_DebeLanzarExcepcion() {
        prepararProducto();

        assertThrows(IllegalArgumentException.class, () -> service.editarProducto(
                authUserId, new EditarProductoCommand(productoId, null, null, BigDecimal.ZERO, null)));
        verify(productoRepositoryPort, never()).update(any(), any());
    }

    @Test
    void crearProducto_CuandoUnidadMedidaEsAlias_DebeNormalizarAlEstandar() {
        when(productoRepositoryPort.existsByIdentificador(any())).thenReturn(false);
        when(productoRepositoryPort.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals("LITRO", service.crearProducto(authUserId,
                new CrearProductoCommand("P-1", "Leche", "Lacteos", new BigDecimal("4000"), "Litro")).unidadMedida());
        assertEquals("UNIDAD", service.crearProducto(authUserId,
                new CrearProductoCommand("P-2", "Pan", "Panaderia", new BigDecimal("1000"), "und")).unidadMedida());
        assertEquals("KG", service.crearProducto(authUserId,
                new CrearProductoCommand("P-3", "Papa", "Verduras", new BigDecimal("3000"), "kilogramos")).unidadMedida());
    }

    @Test
    void crearProducto_CuandoUnidadMedidaEsInvalida_DebeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> service.crearProducto(
                authUserId, new CrearProductoCommand("P-1", "Leche", "Lacteos", new BigDecimal("4000"), "tonelada_x")));
        verify(productoRepositoryPort, never()).save(any());
    }

    @Test
    void editarProducto_CuandoUnidadMedidaEsValidaOInvalida_DebeNormalizarORechazar() {
        Usuario admin = new Usuario(1L, authUserId, "Admin", "admin@opt.com", Rol.ADMINISTRADOR, null);
        when(authorizationService.requireGestionLogistica(authUserId, AuthorizedAction.EDITAR_PRODUCTO))
                .thenReturn(admin);
        prepararProducto();
        when(productoRepositoryPort.update(any(Producto.class), eq(1L)))
                .thenAnswer(inv -> inv.getArgument(0));

        ProductoResponse res = service.editarProducto(authUserId, new EditarProductoCommand(
                productoId, null, null, null, "paquetes"));
        assertEquals("PAQUETE", res.unidadMedida());

        assertThrows(IllegalArgumentException.class, () -> service.editarProducto(
                authUserId, new EditarProductoCommand(productoId, null, null, null, "invalida")));
    }
}
