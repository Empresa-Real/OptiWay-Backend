package com.example.optiway;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import com.example.optiway.application.exception.RecursoNoEncontradoException;
import com.example.optiway.application.port.in.AjustarInventarioCommand;
import com.example.optiway.application.port.in.dto.AjusteInventarioResponse;
import com.example.optiway.application.port.out.AjusteInventarioRepositoryPort;
import com.example.optiway.application.port.out.AuthorizationPort;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.application.service.AjustarInventarioService;
import com.example.optiway.domain.model.AjusteInventario;
import com.example.optiway.domain.model.Inventario;
import com.example.optiway.domain.model.Producto;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.Tienda;
import com.example.optiway.domain.model.Usuario;
import com.example.optiway.infrastructure.security.AuthorizedAction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AjustarInventarioServiceTest {

    @Mock
    private AjusteInventarioRepositoryPort ajusteRepository;

    @Mock
    private InventarioRepositoryPort inventarioRepository;

    @Mock
    private ProductoRepositoryPort productoRepository;

    @Mock
    private TiendaRepositoryPort tiendaRepository;

    @Mock
    private AuthorizationPort authorizationService;

    private AjustarInventarioService service;

    private final UUID authUserId = UUID.randomUUID();

    private final Long tiendaId = 1L;
    private final Long productoId = 2L;

    private Tienda tienda;
    private Usuario usuario;
    private Producto producto;
    private Inventario inventario;

    @BeforeEach
    void setUp() {
        service = new AjustarInventarioService(
                ajusteRepository,
                inventarioRepository,
                productoRepository,
                tiendaRepository,
                authorizationService
        );

        tienda = new Tienda(
                tiendaId, "TND-01", "Tienda Central",
                "Calle 1", "Bogota", true, 10L
        );

        usuario = new Usuario(
                10L, authUserId, "Administrador",
                "admin@optiway.com", Rol.ADMINISTRADOR, null
        );

        producto = new Producto(
                productoId, "PRD-01", "Arroz",
                "Granos", null, "Unidad", true
        );

        inventario = new Inventario(
                100L, tiendaId, null, productoId, 20, 5
        );
    }

    private void prepararEscenarioValido() {
        when(tiendaRepository.obtenerPorId(tiendaId))
                .thenReturn(Optional.of(tienda));

        when(authorizationService.requirePermisoTienda(
                authUserId, tienda, AuthorizedAction.AJUSTAR_INVENTARIO_TIENDA))
                .thenReturn(usuario);

        when(productoRepository.findById(productoId))
                .thenReturn(Optional.of(producto));

        when(inventarioRepository.findByTiendaIdAndProductoId(
                tiendaId, productoId))
                .thenReturn(Optional.of(inventario));

        when(inventarioRepository.save(any(Inventario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(ajusteRepository.guardar(any(AjusteInventario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void ajustarInventario_CuandoAumentaStock_DebeActualizarYRegistrarHistorial() {
        prepararEscenarioValido();

        AjusteInventarioResponse respuesta = service.ajustarInventario(
                authUserId,
                new AjustarInventarioCommand(tiendaId, productoId, 10)
        );

        assertNotNull(respuesta);
        assertEquals(20, respuesta.cantidadAnterior());
        assertEquals(10, respuesta.diferencia());
        assertEquals(30, respuesta.cantidadNueva());
        assertEquals(10L, respuesta.usuarioId());
        assertEquals(30, inventario.getCantidadActual());

        verify(inventarioRepository).save(inventario);
        verify(ajusteRepository).guardar(any(AjusteInventario.class));
    }

    @Test
    void ajustarInventario_CuandoDisminuyeStock_DebeActualizarYRegistrarHistorial() {
        prepararEscenarioValido();

        AjusteInventarioResponse respuesta = service.ajustarInventario(
                authUserId,
                new AjustarInventarioCommand(tiendaId, productoId, -8)
        );

        assertEquals(20, respuesta.cantidadAnterior());
        assertEquals(-8, respuesta.diferencia());
        assertEquals(12, respuesta.cantidadNueva());
        assertEquals(12, inventario.getCantidadActual());

        verify(inventarioRepository).save(inventario);
        verify(ajusteRepository).guardar(any(AjusteInventario.class));
    }

    @Test
    void ajustarInventario_CuandoStockResultanteEsNegativo_DebeRechazarSinGuardar() {
        when(tiendaRepository.obtenerPorId(tiendaId))
                .thenReturn(Optional.of(tienda));

        when(authorizationService.requirePermisoTienda(
                authUserId, tienda, AuthorizedAction.AJUSTAR_INVENTARIO_TIENDA))
                .thenReturn(usuario);

        when(productoRepository.findById(productoId))
                .thenReturn(Optional.of(producto));

        when(inventarioRepository.findByTiendaIdAndProductoId(
                tiendaId, productoId))
                .thenReturn(Optional.of(inventario));

        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> service.ajustarInventario(
                        authUserId,
                        new AjustarInventarioCommand(tiendaId, productoId, -25)
                )
        );

        assertTrue(excepcion.getMessage().contains("cantidad negativa"));
        assertEquals(20, inventario.getCantidadActual());

        verify(inventarioRepository, never()).save(any());
        verify(ajusteRepository, never()).guardar(any());
    }

    @Test
    void ajustarInventario_CuandoUsuarioNoTienePermiso_DebeRechazarSinGuardar() {
        when(tiendaRepository.obtenerPorId(tiendaId))
                .thenReturn(Optional.of(tienda));

        doThrow(new IllegalStateException("Acceso denegado"))
                .when(authorizationService)
                .requirePermisoTienda(
                        authUserId, tienda,
                        AuthorizedAction.AJUSTAR_INVENTARIO_TIENDA
                );

        assertThrows(
                IllegalStateException.class,
                () -> service.ajustarInventario(
                        authUserId,
                        new AjustarInventarioCommand(tiendaId, productoId, 5)
                )
        );

        verifyNoInteractions(productoRepository, inventarioRepository, ajusteRepository);
    }

    @Test
    void ajustarInventario_CuandoTiendaNoExiste_DebeLanzarExcepcion() {
        when(tiendaRepository.obtenerPorId(tiendaId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.ajustarInventario(
                        authUserId,
                        new AjustarInventarioCommand(tiendaId, productoId, 5)
                )
        );

        verifyNoInteractions(authorizationService, productoRepository,
                inventarioRepository, ajusteRepository);
    }

    @Test
    void ajustarInventario_CuandoProductoNoExiste_DebeLanzarExcepcion() {
        when(tiendaRepository.obtenerPorId(tiendaId))
                .thenReturn(Optional.of(tienda));

        when(authorizationService.requirePermisoTienda(
                authUserId, tienda, AuthorizedAction.AJUSTAR_INVENTARIO_TIENDA))
                .thenReturn(usuario);

        when(productoRepository.findById(productoId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.ajustarInventario(
                        authUserId,
                        new AjustarInventarioCommand(tiendaId, productoId, 5)
                )
        );

        verifyNoInteractions(inventarioRepository, ajusteRepository);
    }

    @Test
    void ajustarInventario_CuandoInventarioNoExiste_DebeLanzarExcepcion() {
        when(tiendaRepository.obtenerPorId(tiendaId))
                .thenReturn(Optional.of(tienda));

        when(authorizationService.requirePermisoTienda(
                authUserId, tienda, AuthorizedAction.AJUSTAR_INVENTARIO_TIENDA))
                .thenReturn(usuario);

        when(productoRepository.findById(productoId))
                .thenReturn(Optional.of(producto));

        when(inventarioRepository.findByTiendaIdAndProductoId(
                tiendaId, productoId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.ajustarInventario(
                        authUserId,
                        new AjustarInventarioCommand(tiendaId, productoId, 5)
                )
        );

        verify(inventarioRepository, never()).save(any());
        verifyNoInteractions(ajusteRepository);
    }

    @Test
    void ajustarInventario_CuandoDiferenciaEsCero_DebeRechazarLaOperacion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.ajustarInventario(
                        authUserId,
                        new AjustarInventarioCommand(tiendaId, productoId, 0)
                )
        );

        verifyNoInteractions(
                tiendaRepository,
                authorizationService,
                productoRepository,
                inventarioRepository,
                ajusteRepository
        );
    }
}
