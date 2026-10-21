package com.example.optiway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.example.optiway.application.port.in.RegistrarVentaCommand;
import com.example.optiway.application.port.in.dto.VentaResponse;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.application.port.out.VentaRepositoryPort;
import com.example.optiway.application.exception.AccesoDenegadoException;
import com.example.optiway.application.exception.RecursoNoEncontradoException;
import com.example.optiway.application.exception.StockInsuficienteException;
import com.example.optiway.application.service.VentaService;
import com.example.optiway.domain.model.Inventario;
import com.example.optiway.domain.model.Producto;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.Tienda;
import com.example.optiway.domain.model.Usuario;
import com.example.optiway.domain.model.Venta;
import com.example.optiway.infrastructure.security.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaRepositoryPort ventaRepositoryPort;

    @Mock
    private InventarioRepositoryPort inventarioRepositoryPort;

    @Mock
    private TiendaRepositoryPort tiendaRepositoryPort;

    @Mock
    private ProductoRepositoryPort productoRepositoryPort;

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    private VentaService ventaService;

    @BeforeEach
    void setUp() {
        AuthorizationService authService = new AuthorizationService(usuarioRepositoryPort);
        ventaService = new VentaService(
                ventaRepositoryPort,
                inventarioRepositoryPort,
                tiendaRepositoryPort,
                productoRepositoryPort,
                authService
        );
    }

    @Test
    void registrarVenta_exitoso_descuentaStockYGuardaVenta() {
        Long tiendaId = 1L;
        Long productoId = 10L;
        UUID authUserId = UUID.randomUUID();
        Long encargadoId = 100L;

        Tienda tienda = new Tienda(tiendaId, "TND-001", "Tienda Norte", "Calle 10", "Medellin", true, encargadoId);
        Usuario usuario = new Usuario(encargadoId, authUserId, "Carlos", "carlos@optiway.com", Rol.ENCARGADO_TIENDA, null);

        Producto producto = new Producto(productoId, "PROD-001", "Camisa", "Ropa", BigDecimal.valueOf(50.0), "Unidad", true);
        Inventario inventario = new Inventario(1L, tiendaId, null, productoId, 15, 5);

        when(tiendaRepositoryPort.obtenerPorId(tiendaId)).thenReturn(Optional.of(tienda));
        when(usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)).thenReturn(Optional.of(usuario));
        when(productoRepositoryPort.findById(productoId)).thenReturn(Optional.of(producto));
        when(inventarioRepositoryPort.findByTiendaIdAndProductoId(tiendaId, productoId)).thenReturn(Optional.of(inventario));
        when(ventaRepositoryPort.guardar(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegistrarVentaCommand command = new RegistrarVentaCommand(tiendaId, productoId, 3, LocalDateTime.now());

        VentaResponse resultado = ventaService.registrarVenta(authUserId, command);

        assertNotNull(resultado);
        assertEquals(tiendaId, resultado.tiendaId());
        assertEquals(productoId, resultado.productoId());
        assertEquals(3, resultado.cantidad());

        // Verificar que el stock fue descontado (15 - 3 = 12)
        ArgumentCaptor<Inventario> captor = ArgumentCaptor.forClass(Inventario.class);
        verify(inventarioRepositoryPort).save(captor.capture());
        assertEquals(12, captor.getValue().getCantidadActual());
    }

    @Test
    void registrarVenta_stockInsuficiente_lanzaExcepcion() {
        Long tiendaId = 1L;
        Long productoId = 10L;
        UUID authUserId = UUID.randomUUID();

        Tienda tienda = new Tienda(tiendaId, "TND-001", "Tienda Norte", "Calle 10", "Medellin", true, 100L);
        Usuario admin = new Usuario(1L, authUserId, "Admin", "admin@optiway.com", Rol.ADMINISTRADOR, null);
        Producto producto = new Producto(productoId, "PROD-001", "Camisa", "Ropa", BigDecimal.valueOf(50.0), "Unidad", true);
        Inventario inventario = new Inventario(1L, tiendaId, null, productoId, 2, 5);

        when(tiendaRepositoryPort.obtenerPorId(tiendaId)).thenReturn(Optional.of(tienda));
        when(usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)).thenReturn(Optional.of(admin));
        when(productoRepositoryPort.findById(productoId)).thenReturn(Optional.of(producto));
        when(inventarioRepositoryPort.findByTiendaIdAndProductoId(tiendaId, productoId)).thenReturn(Optional.of(inventario));

        RegistrarVentaCommand command = new RegistrarVentaCommand(tiendaId, productoId, 5, LocalDateTime.now());

        assertThrows(StockInsuficienteException.class, () -> ventaService.registrarVenta(authUserId, command));
        verify(inventarioRepositoryPort, never()).save(any());
        verify(ventaRepositoryPort, never()).guardar(any());
    }

    @Test
    void registrarVenta_productoInactivo_lanzaExcepcion() {
        Long tiendaId = 1L;
        Long productoId = 10L;
        UUID authUserId = UUID.randomUUID();

        Tienda tienda = new Tienda(tiendaId, "TND-001", "Tienda Norte", "Calle 10", "Medellin", true, 100L);
        Usuario admin = new Usuario(1L, authUserId, "Admin", "admin@optiway.com", Rol.ADMINISTRADOR, null);
        Producto producto = new Producto(productoId, "PROD-001", "Camisa", "Ropa", BigDecimal.valueOf(50.0), "Unidad", false); // Inactivo

        when(tiendaRepositoryPort.obtenerPorId(tiendaId)).thenReturn(Optional.of(tienda));
        when(usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)).thenReturn(Optional.of(admin));
        when(productoRepositoryPort.findById(productoId)).thenReturn(Optional.of(producto));

        RegistrarVentaCommand command = new RegistrarVentaCommand(tiendaId, productoId, 1, LocalDateTime.now());

        assertThrows(IllegalArgumentException.class, () -> ventaService.registrarVenta(authUserId, command));
        verify(inventarioRepositoryPort, never()).save(any());
    }

    @Test
    void registrarVenta_permisosInsuficientes_lanzaAccesoDenegado() {
        Long tiendaId = 1L;
        Long productoId = 10L;
        UUID authUserId = UUID.randomUUID();

        Tienda tienda = new Tienda(tiendaId, "TND-001", "Tienda Norte", "Calle 10", "Medellin", true, 100L);
        Usuario usuarioAjeno = new Usuario(200L, authUserId, "Pedro", "pedro@optiway.com", Rol.ENCARGADO_TIENDA, null); // Encargado de OTRA tienda

        when(tiendaRepositoryPort.obtenerPorId(tiendaId)).thenReturn(Optional.of(tienda));
        when(usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)).thenReturn(Optional.of(usuarioAjeno));

        RegistrarVentaCommand command = new RegistrarVentaCommand(tiendaId, productoId, 1, LocalDateTime.now());

        assertThrows(AccesoDenegadoException.class, () -> ventaService.registrarVenta(authUserId, command));
        verify(ventaRepositoryPort, never()).guardar(any());
    }
}
