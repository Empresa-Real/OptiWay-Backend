package com.example.optiway.unit.application;

import com.example.optiway.application.exception.AccesoDenegadoException;
import com.example.optiway.application.exception.RecursoNoEncontradoException;
import com.example.optiway.application.port.in.dto.ReporteRotacionProductoResponse;
import com.example.optiway.application.port.in.dto.ReporteRotacionResponse;
import com.example.optiway.application.port.out.AuthorizationPort;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.application.port.out.VentaProductoResumen;
import com.example.optiway.application.port.out.VentaRepositoryPort;
import com.example.optiway.application.service.ReporteRotacionService;
import com.example.optiway.domain.model.Inventario;
import com.example.optiway.domain.model.Producto;
import com.example.optiway.domain.model.Tienda;
import com.example.optiway.infrastructure.security.AuthorizedAction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ReporteRotacionServiceTest {

    private final VentaRepositoryPort ventaRepository = mock(VentaRepositoryPort.class);
    private final InventarioRepositoryPort inventarioRepository = mock(InventarioRepositoryPort.class);
    private final ProductoRepositoryPort productoRepository = mock(ProductoRepositoryPort.class);
    private final TiendaRepositoryPort tiendaRepository = mock(TiendaRepositoryPort.class);
    private final AuthorizationPort authorization = mock(AuthorizationPort.class);

    private final ReporteRotacionService service = new ReporteRotacionService(
            ventaRepository, inventarioRepository, productoRepository, tiendaRepository, authorization);

    private final UUID authUserId = UUID.randomUUID();
    private final Long tiendaId = 1L;
    private final LocalDate desde = LocalDate.of(2026, 11, 1);
    private final LocalDate hasta = LocalDate.of(2026, 11, 10);

    @Test
    void generaReporteConCalculoDeDiasPromedioMedianaYClasificacion() {
        when(tiendaRepository.obtenerPorId(tiendaId)).thenReturn(Optional.of(mock(Tienda.class)));
        when(ventaRepository.resumirUnidadesVendidasPorProducto(
                tiendaId, desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(
                        new VentaProductoResumen(10L, 20L),
                        new VentaProductoResumen(20L, 5L)
                ));
        when(inventarioRepository.findByTiendaId(tiendaId)).thenReturn(List.of(
                new Inventario(1L, tiendaId, null, 10L, 10, 2),
                new Inventario(2L, tiendaId, null, 20L, 10, 2),
                new Inventario(3L, tiendaId, null, 30L, 15, 2)
        ));
        when(productoRepository.findAllById(List.of(10L, 20L, 30L))).thenReturn(List.of(
                new Producto(10L, "P-10", "Arroz", "Granos", new BigDecimal("5000"), "Unidad", true),
                new Producto(20L, "P-20", "Azucar", "Granos", new BigDecimal("4000"), "Unidad", true),
                new Producto(30L, "P-30", "Sal", "Granos", new BigDecimal("2000"), "Unidad", true)
        ));

        ReporteRotacionResponse reporte = service.generarReporte(authUserId, tiendaId, desde, hasta);

        assertEquals(tiendaId, reporte.tiendaId());
        assertEquals(3, reporte.productos().size());

        ReporteRotacionProductoResponse primero = reporte.productos().get(0);
        assertEquals(10L, primero.productoId());
        assertEquals(20L, primero.unidadesVendidas());
        assertEquals(10, primero.stockActual());
        assertEquals(new BigDecimal("10.00"), primero.tiempoPromedioEstimadoEnInventarioDias());
        assertEquals("RAPIDA", primero.clasificacionRotacion());

        ReporteRotacionProductoResponse segundo = reporte.productos().get(1);
        assertEquals(20L, segundo.productoId());
        assertEquals(5L, segundo.unidadesVendidas());
        assertEquals(new BigDecimal("25.00"), segundo.tiempoPromedioEstimadoEnInventarioDias());
        assertEquals("BAJA", segundo.clasificacionRotacion());

        ReporteRotacionProductoResponse tercero = reporte.productos().get(2);
        assertEquals(30L, tercero.productoId());
        assertEquals(0L, tercero.unidadesVendidas());
        assertNull(tercero.tiempoPromedioEstimadoEnInventarioDias());
        assertEquals("SIN_VENTAS", tercero.clasificacionRotacion());

        verify(authorization).requirePlanificador(authUserId, AuthorizedAction.CONSULTAR_REPORTE_ROTACION);
    }

    @Test
    void rechazaParametrosInvalidosYLocalDateMax() {
        assertThrows(IllegalArgumentException.class, () ->
                service.generarReporte(authUserId, null, desde, hasta));
        assertThrows(IllegalArgumentException.class, () ->
                service.generarReporte(authUserId, 0L, desde, hasta));
        assertThrows(IllegalArgumentException.class, () ->
                service.generarReporte(authUserId, tiendaId, null, hasta));
        assertThrows(IllegalArgumentException.class, () ->
                service.generarReporte(authUserId, tiendaId, desde, null));
        assertThrows(IllegalArgumentException.class, () ->
                service.generarReporte(authUserId, tiendaId, hasta, desde));
        assertThrows(IllegalArgumentException.class, () ->
                service.generarReporte(authUserId, tiendaId, desde, LocalDate.MAX));
        verifyNoInteractions(authorization, tiendaRepository, ventaRepository, inventarioRepository, productoRepository);
    }

    @Test
    void lanzaRecursoNoEncontradoCuandoTiendaNoExiste() {
        when(tiendaRepository.obtenerPorId(tiendaId)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () ->
                service.generarReporte(authUserId, tiendaId, desde, hasta));
        verifyNoInteractions(ventaRepository, inventarioRepository, productoRepository);
    }

    @Test
    void rechazaAccesoCuandoUsuarioNoEsPlanificador() {
        doThrow(new AccesoDenegadoException("Solo planificadores"))
                .when(authorization)
                .requirePlanificador(authUserId, AuthorizedAction.CONSULTAR_REPORTE_ROTACION);

        assertThrows(AccesoDenegadoException.class, () ->
                service.generarReporte(authUserId, tiendaId, desde, hasta));
        verifyNoInteractions(tiendaRepository, ventaRepository, inventarioRepository, productoRepository);
    }
}
