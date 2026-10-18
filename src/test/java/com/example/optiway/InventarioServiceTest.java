package com.example.optiway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.optiway.application.port.out.CentroDistribucionRepositoryPort;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.application.service.AccesoDenegadoException;
import com.example.optiway.application.service.InventarioService;
import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.domain.model.Inventario;
import com.example.optiway.domain.model.Producto;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.Tienda;
import com.example.optiway.domain.model.Usuario;
import com.example.optiway.infrastructure.security.AuthorizationService;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private InventarioRepositoryPort inventarioRepositoryPort;

    @Mock
    private ProductoRepositoryPort productoRepositoryPort;

    @Mock
    private TiendaRepositoryPort tiendaRepositoryPort;

    @Mock
    private CentroDistribucionRepositoryPort centroDistribucionRepositoryPort;

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    private InventarioService inventarioService;

    @BeforeEach
    void setUp() {
        AuthorizationService authorizationService = new AuthorizationService(usuarioRepositoryPort);
        inventarioService = new InventarioService(
                inventarioRepositoryPort,
                productoRepositoryPort,
                tiendaRepositoryPort,
                centroDistribucionRepositoryPort,
                authorizationService
        );
    }

    @Test
    void consultarInventario_CuandoTiendaExiste_DebeRetornarInventariosEnriquecidos() {
        UUID authUserId = UUID.randomUUID();
        Long tiendaId = 1L;

        Usuario usuario = new Usuario(10L, null, "Admin", "admin@optiway.com", Rol.ADMINISTRADOR, null);
        Tienda tienda = new Tienda(tiendaId, "TND-01", "Tienda Central", "Calle 1", "Bogota", true, 10L);

        Inventario inv1 = new Inventario();
        inv1.setId(100L);
        inv1.setTiendaId(tiendaId);
        inv1.setProductoId(1L);
        inv1.setCantidadActual(5);
        inv1.setStockMinimo(10);

        Producto prod1 = new Producto(1L, "PRD-01", "Arroz Diana 1kg", "Granos", new BigDecimal("4500"), null, true);

        when(tiendaRepositoryPort.obtenerPorId(tiendaId)).thenReturn(Optional.of(tienda));
        when(usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)).thenReturn(Optional.of(usuario));
        when(inventarioRepositoryPort.findByTiendaId(tiendaId)).thenReturn(List.of(inv1));
        when(productoRepositoryPort.findAllById(List.of(1L))).thenReturn(List.of(prod1));

        List<Inventario> resultado = inventarioService.consultarInventario(
                authUserId, tiendaId, null, null, null, null);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        Inventario item = resultado.get(0);
        assertEquals("Arroz Diana 1kg", item.getNombreProducto());
        assertEquals("Granos", item.getCategoriaProducto());
        assertTrue(item.isStockBajo(), "El stock bajo debe ser true cuando cantidadActual <= stockMinimo");
    }

    @Test
    void consultarInventario_CuandoFiltraPorNombreYCategoria_DebeRetornarSoloCoincidentes() {
        UUID authUserId = UUID.randomUUID();
        Long tiendaId = 1L;

        Usuario usuario = new Usuario(10L, null, "Admin", "admin@optiway.com", Rol.ADMINISTRADOR, null);
        Tienda tienda = new Tienda(tiendaId, "TND-01", "Tienda Central", "Calle 1", "Bogota", true, 10L);

        Inventario inv1 = new Inventario();
        inv1.setId(101L);
        inv1.setTiendaId(tiendaId);
        inv1.setProductoId(1L);
        inv1.setCantidadActual(20);
        inv1.setStockMinimo(5);

        Inventario inv2 = new Inventario();
        inv2.setId(102L);
        inv2.setTiendaId(tiendaId);
        inv2.setProductoId(2L);
        inv2.setCantidadActual(15);
        inv2.setStockMinimo(5);

        Producto prod1 = new Producto(1L, "PRD-01", "Arroz Diana", "Granos", new BigDecimal("4500"), null, true);
        Producto prod2 = new Producto(2L, "PRD-02", "Aceite Premier", "Aceites", new BigDecimal("12000"), null, true);

        when(tiendaRepositoryPort.obtenerPorId(tiendaId)).thenReturn(Optional.of(tienda));
        when(usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)).thenReturn(Optional.of(usuario));
        when(inventarioRepositoryPort.findByTiendaId(tiendaId)).thenReturn(List.of(inv1, inv2));
        when(productoRepositoryPort.findAllById(List.of(1L, 2L))).thenReturn(List.of(prod1, prod2));

        List<Inventario> resultado = inventarioService.consultarInventario(
                authUserId, tiendaId, null, "arroz", "Granos", null);

        assertEquals(1, resultado.size());
        assertEquals("Arroz Diana", resultado.get(0).getNombreProducto());
        assertFalse(resultado.get(0).isStockBajo());
    }

    @Test
    void consultarInventario_CuandoFiltraPorProductoId_DebeConsultarSoloEseProducto() {
        UUID authUserId = UUID.randomUUID();
        Long tiendaId = 1L;
        Long productoId = 1L;

        Usuario usuario = new Usuario(10L, null, "Admin", "admin@optiway.com", Rol.ADMINISTRADOR, null);
        Tienda tienda = new Tienda(tiendaId, "TND-01", "Tienda Central", "Calle 1", "Bogota", true, 10L);

        Inventario inv1 = new Inventario();
        inv1.setId(101L);
        inv1.setTiendaId(tiendaId);
        inv1.setProductoId(productoId);
        inv1.setCantidadActual(20);
        inv1.setStockMinimo(5);

        Producto prod1 = new Producto(productoId, "PRD-01", "Arroz Diana", "Granos", new BigDecimal("4500"), null, true);

        when(tiendaRepositoryPort.obtenerPorId(tiendaId)).thenReturn(Optional.of(tienda));
        when(usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)).thenReturn(Optional.of(usuario));
        when(inventarioRepositoryPort.findByTiendaIdAndProductoId(tiendaId, productoId)).thenReturn(Optional.of(inv1));
        when(productoRepositoryPort.findAllById(List.of(productoId))).thenReturn(List.of(prod1));

        List<Inventario> resultado = inventarioService.consultarInventario(
                authUserId, tiendaId, null, null, null, productoId);

        assertEquals(1, resultado.size());
        verify(inventarioRepositoryPort).findByTiendaIdAndProductoId(tiendaId, productoId);
        verify(inventarioRepositoryPort, never()).findByTiendaId(tiendaId);
    }

    @Test
    void consultarInventario_CuandoTiendaNoExiste_DebeLanzarExcepcion() {
        UUID authUserId = UUID.randomUUID();
        when(tiendaRepositoryPort.obtenerPorId(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> inventarioService.consultarInventario(authUserId, 99L, null, null, null, null)
        );

        assertEquals("La tienda no existe.", ex.getMessage());
        verifyNoInteractions(productoRepositoryPort, inventarioRepositoryPort);
    }

    @Test
    void consultarInventario_CuandoUsuarioNoTienePermisoTienda_DebeLanzarAccesoDenegado() {
        UUID authUserId = UUID.randomUUID();
        Long tiendaId = 1L;

        // Encargado de otra tienda (no asignado a tiendaId)
        Usuario usuario = new Usuario(20L, null, "Encargado Otro", "otro@optiway.com", Rol.ENCARGADO_TIENDA, null);
        Tienda tienda = new Tienda(tiendaId, "TND-01", "Tienda Central", "Calle 1", "Bogota", true, 10L);

        when(tiendaRepositoryPort.obtenerPorId(tiendaId)).thenReturn(Optional.of(tienda));
        when(usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)).thenReturn(Optional.of(usuario));

        assertThrows(
                AccesoDenegadoException.class,
                () -> inventarioService.consultarInventario(authUserId, tiendaId, null, null, null, null)
        );

        verifyNoInteractions(productoRepositoryPort, inventarioRepositoryPort);
    }

    @Test
    void consultarInventario_CuandoCDExiste_DebeRetornarInventariosDeCD() {
        UUID authUserId = UUID.randomUUID();
        Long cdId = 5L;

        Usuario usuario = new Usuario(30L, null, "Encargado CD", "cd@optiway.com", Rol.ENCARGADO_CD, null);
        CentroDistribucion cd = new CentroDistribucion(cdId, "CD-01", "Hub Norte", "Via 40", 5000, 30L, List.of());

        Inventario invCD = new Inventario();
        invCD.setId(201L);
        invCD.setCentroDistribucionId(cdId);
        invCD.setProductoId(3L);
        invCD.setCantidadActual(500);
        invCD.setStockMinimo(100);

        Producto prod3 = new Producto(3L, "PRD-03", "Azúcar 1kg", "Endulzantes", new BigDecimal("3800"), null, true);

        when(centroDistribucionRepositoryPort.obtenerPorId(cdId)).thenReturn(Optional.of(cd));
        when(usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)).thenReturn(Optional.of(usuario));
        when(inventarioRepositoryPort.findByCentroDistribucionId(cdId)).thenReturn(List.of(invCD));
        when(productoRepositoryPort.findAllById(List.of(3L))).thenReturn(List.of(prod3));

        List<Inventario> resultado = inventarioService.consultarInventario(
                authUserId, null, cdId, null, null, null);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Azúcar 1kg", resultado.get(0).getNombreProducto());
        assertEquals(cdId, resultado.get(0).getCentroDistribucionId());
    }

    @Test
    void consultarInventario_CuandoCDNoExiste_DebeLanzarExcepcion() {
        UUID authUserId = UUID.randomUUID();
        when(centroDistribucionRepositoryPort.obtenerPorId(88L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> inventarioService.consultarInventario(authUserId, null, 88L, null, null, null)
        );

        assertEquals("El centro de distribución no existe", ex.getMessage());
        verifyNoInteractions(productoRepositoryPort, inventarioRepositoryPort);
    }

    @Test
    void consultarInventario_CuandoInventarioEstaVacio_DebeRetornarListaVaciaSinCargarProductos() {
        UUID authUserId = UUID.randomUUID();
        Long tiendaId = 1L;

        Usuario usuario = new Usuario(10L, null, "Admin", "admin@optiway.com", Rol.ADMINISTRADOR, null);
        Tienda tienda = new Tienda(tiendaId, "TND-01", "Tienda Central", "Calle 1", "Bogota", true, 10L);

        when(tiendaRepositoryPort.obtenerPorId(tiendaId)).thenReturn(Optional.of(tienda));
        when(usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)).thenReturn(Optional.of(usuario));
        when(inventarioRepositoryPort.findByTiendaId(tiendaId)).thenReturn(List.of());

        List<Inventario> resultado = inventarioService.consultarInventario(
                authUserId, tiendaId, null, null, null, null);

        assertTrue(resultado.isEmpty());
        verifyNoInteractions(productoRepositoryPort);
    }
}
