package com.example.optiway;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.optiway.application.port.in.AjustarInventarioCommand;
import com.example.optiway.application.port.in.dto.AjusteInventarioResponse;
import com.example.optiway.application.port.out.AjusteInventarioRepositoryPort;
import com.example.optiway.application.port.out.AuthorizationPort;
import com.example.optiway.application.port.out.CentroDistribucionRepositoryPort;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.application.service.AjustarInventarioService;
import com.example.optiway.domain.model.AjusteInventario;
import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.domain.model.Inventario;
import com.example.optiway.domain.model.Producto;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.Tienda;
import com.example.optiway.domain.model.Usuario;
import com.example.optiway.infrastructure.security.AuthorizedAction;

@ExtendWith(MockitoExtension.class)
class AjustarInventarioServiceTest {

    @Mock private AjusteInventarioRepositoryPort ajusteRepository;
    @Mock private InventarioRepositoryPort inventarioRepository;
    @Mock private ProductoRepositoryPort productoRepository;
    @Mock private TiendaRepositoryPort tiendaRepository;
    @Mock private CentroDistribucionRepositoryPort centroRepository;
    @Mock private AuthorizationPort authorizationPort;

    private AjustarInventarioService service;

    private final UUID authUserId = UUID.randomUUID();
    private final Long productoId = 10L;
    private final Long tiendaId = 20L;
    private final Long cdId = 30L;

    private Producto producto;
    private Tienda tienda;
    private CentroDistribucion centro;
    private Usuario encargadoTienda;
    private Usuario encargadoCD;

    @BeforeEach
    void setUp() {
        service = new AjustarInventarioService(
                ajusteRepository,
                inventarioRepository,
                productoRepository,
                tiendaRepository,
                centroRepository,
                authorizationPort
        );

        producto = new Producto(productoId, "PROD-10", "Camisa", "Ropa", new BigDecimal("50000"), "Unidad", true);
        tienda = new Tienda(tiendaId, "TND-20", "Tienda Sur", "Calle 1", "Medellín", true, 100L);
        centro = new CentroDistribucion(cdId, "CD-30", "CD Norte", "Autopista", 5000, 200L, List.of(tiendaId));
        encargadoTienda = new Usuario(100L, authUserId, "Ana", "ana@optiway.com", Rol.ENCARGADO_TIENDA, null);
        encargadoCD = new Usuario(200L, authUserId, "Luis", "luis@optiway.com", Rol.ENCARGADO_CD, null);
    }

    @Test
    void ajustarInventarioTienda_CuandoConteoEsValido_DebeActualizarYGuardarHistorial() {
        Inventario inventario = new Inventario(1L, tiendaId, null, productoId, 40, 5);

        when(tiendaRepository.obtenerPorId(tiendaId)).thenReturn(Optional.of(tienda));
        when(authorizationPort.requirePermisoTienda(authUserId, tienda, AuthorizedAction.AJUSTAR_INVENTARIO_TIENDA))
                .thenReturn(encargadoTienda);
        when(productoRepository.findById(productoId)).thenReturn(Optional.of(producto));
        when(inventarioRepository.findByTiendaIdAndProductoId(tiendaId, productoId))
                .thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ajusteRepository.guardar(any(AjusteInventario.class))).thenAnswer(inv -> {
            AjusteInventario a = inv.getArgument(0);
            a.setId(500L);
            return a;
        });

        AjusteInventarioResponse res = service.ajustarInventario(
                authUserId, new AjustarInventarioCommand(tiendaId, null, productoId, 32));

        assertEquals(500L, res.id());
        assertEquals(tiendaId, res.tiendaId());
        assertNull(res.centroDistribucionId());
        assertEquals(40, res.cantidadAnterior());
        assertEquals(-8, res.diferencia());
        assertEquals(32, res.cantidadNueva());
        assertEquals(100L, res.usuarioId());
        assertEquals(32, inventario.getCantidadActual());
        assertNotNull(inventario.getFechaActualizacion());
        verify(ajusteRepository).guardar(any(AjusteInventario.class));
    }

    @Test
    void ajustarInventarioCD_CuandoConteoEsValido_DebeActualizarYGuardarHistorial() {
        Inventario inventario = new Inventario(2L, null, cdId, productoId, 100, 20);

        when(centroRepository.obtenerPorId(cdId)).thenReturn(Optional.of(centro));
        when(authorizationPort.requireEncargadoDelCentro(authUserId, centro, AuthorizedAction.AJUSTAR_INVENTARIO_CENTRO_DISTRIBUCION))
                .thenReturn(encargadoCD);
        when(productoRepository.findById(productoId)).thenReturn(Optional.of(producto));
        when(inventarioRepository.findByCentroDistribucionIdAndProductoId(cdId, productoId))
                .thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ajusteRepository.guardar(any(AjusteInventario.class))).thenAnswer(inv -> {
            AjusteInventario a = inv.getArgument(0);
            a.setId(501L);
            return a;
        });

        AjusteInventarioResponse res = service.ajustarInventario(
                authUserId, new AjustarInventarioCommand(null, cdId, productoId, 125));

        assertEquals(501L, res.id());
        assertNull(res.tiendaId());
        assertEquals(cdId, res.centroDistribucionId());
        assertEquals(100, res.cantidadAnterior());
        assertEquals(25, res.diferencia());
        assertEquals(125, res.cantidadNueva());
        assertEquals(200L, res.usuarioId());
        assertEquals(125, inventario.getCantidadActual());
        verify(ajusteRepository).guardar(any(AjusteInventario.class));
    }

    @Test
    void ajustarInventario_CuandoNuevaCantidadEsNegativa_DebeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () ->
                service.ajustarInventario(authUserId, new AjustarInventarioCommand(tiendaId, null, productoId, -1)));
        verifyNoInteractions(inventarioRepository, ajusteRepository);
    }

    @Test
    void ajustarInventario_CuandoUbicacionEsAmbiguaONula_DebeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () ->
                service.ajustarInventario(authUserId, new AjustarInventarioCommand(tiendaId, cdId, productoId, 10)));
        assertThrows(IllegalArgumentException.class, () ->
                service.ajustarInventario(authUserId, new AjustarInventarioCommand(null, null, productoId, 10)));
        verifyNoInteractions(inventarioRepository, ajusteRepository);
    }
}

