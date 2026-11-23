package com.example.optiway;

import com.example.optiway.application.exception.AccesoDenegadoException;
import com.example.optiway.application.exception.RecursoNoEncontradoException;
import com.example.optiway.application.port.in.dto.PatronesDemandaResponse;
import com.example.optiway.application.port.in.dto.PatronesDemandaResponse.DemandaTienda;
import com.example.optiway.application.port.in.dto.PatronesDemandaResponse.VentaDiaria;
import com.example.optiway.application.port.out.AuthorizationPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.application.port.out.VentaRepositoryPort;
import com.example.optiway.application.service.ConsultarPatronesDemandaService;
import com.example.optiway.domain.model.Producto;
import com.example.optiway.domain.model.Tienda;
import com.example.optiway.domain.model.Venta;
import com.example.optiway.infrastructure.security.AuthorizedAction;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ConsultarPatronesDemandaServiceTest {

    private final AuthorizationPort autorizacion = mock(AuthorizationPort.class);
    private final ProductoRepositoryPort productos = mock(ProductoRepositoryPort.class);
    private final TiendaRepositoryPort tiendas = mock(TiendaRepositoryPort.class);
    private final VentaRepositoryPort ventas = mock(VentaRepositoryPort.class);

    private final ConsultarPatronesDemandaService servicio =
            new ConsultarPatronesDemandaService(autorizacion, productos, tiendas, ventas);

    private final UUID usuario = UUID.randomUUID();
    private final Long productoId = 3L;
    private final LocalDate desde = LocalDate.of(2026, 11, 1);
    private final LocalDate hasta = desde.plusDays(2);

    @Test
    void sumaLasVentasDelDiaEIncluyeLosDiasSinVentas() {
        var resultado = consultar(List.of(1L), List.of(
                venta(1L, desde, 2),
                venta(1L, desde, 3),
                venta(1L, hasta, 4)
        ));

        DemandaTienda tienda = resultado.tiendas().get(0);
        assertEquals(9L, tienda.unidadesVendidas());
        assertEquals(List.of(
                new VentaDiaria(desde, 5L),
                new VentaDiaria(desde.plusDays(1), 0L),
                new VentaDiaria(hasta, 4L)
        ), tienda.evolucion());

        verify(autorizacion).requirePlanificador(
                usuario, AuthorizedAction.CONSULTAR_PATRONES_DEMANDA);
    }

    @Test
    void comparaTiendasEIdentificaLaMayorYLaMenorRotacion() {
        var resultado = consultar(List.of(1L, 2L, 3L), List.of(
                venta(1L, desde, 8),
                venta(2L, desde, 10),
                venta(2L, hasta, 5)
        ));

        assertEquals(List.of(8L, 15L, 0L), resultado.tiendas().stream()
                .map(DemandaTienda::unidadesVendidas)
                .toList());
        assertEquals(List.of(2L), resultado.tiendasMayorRotacion());
        assertEquals(List.of(3L), resultado.tiendasMenorRotacion());
    }

    @Test
    void incluyeTodasLasTiendasEmpatadasEnLaMayorRotacion() {
        var resultado = consultar(List.of(1L, 2L, 3L), List.of(
                venta(1L, desde, 7),
                venta(2L, hasta, 7),
                venta(3L, desde, 2)
        ));

        assertEquals(List.of(1L, 2L), resultado.tiendasMayorRotacion());
        assertEquals(List.of(3L), resultado.tiendasMenorRotacion());
    }

    @Test
    void deduplicaTiendasRepetidasYConsultaUnaSolaVezPorTienda() {
        when(productos.findById(productoId)).thenReturn(Optional.of(mock(Producto.class)));
        when(tiendas.obtenerPorId(1L)).thenReturn(Optional.of(mock(Tienda.class)));
        when(tiendas.obtenerPorId(2L)).thenReturn(Optional.of(mock(Tienda.class)));
        when(ventas.buscarVentasPorProductoYTiendasEnPeriodo(
                productoId, List.of(1L, 2L), desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(venta(1L, desde, 4)));

        var resultado = servicio.consultarPatronesDemanda(
                usuario, productoId, List.of(1L, 2L, 1L), desde, hasta);

        assertEquals(2, resultado.tiendas().size());
        verify(tiendas, times(1)).obtenerPorId(1L);
        verify(tiendas, times(1)).obtenerPorId(2L);
    }

    @Test
    void rechazaProductoInvalido() {
        assertThrows(IllegalArgumentException.class, () ->
                servicio.consultarPatronesDemanda(usuario, null, List.of(1L), desde, hasta));
        assertThrows(IllegalArgumentException.class, () ->
                servicio.consultarPatronesDemanda(usuario, 0L, List.of(1L), desde, hasta));
        verifyNoInteractions(productos, tiendas, ventas);
    }

    @Test
    void rechazaListaDeTiendasVaciaNulaOConIdsInvalidos() {
        assertThrows(IllegalArgumentException.class, () ->
                servicio.consultarPatronesDemanda(usuario, productoId, null, desde, hasta));
        assertThrows(IllegalArgumentException.class, () ->
                servicio.consultarPatronesDemanda(usuario, productoId, List.of(), desde, hasta));
        assertThrows(IllegalArgumentException.class, () ->
                servicio.consultarPatronesDemanda(usuario, productoId, Arrays.asList(1L, null), desde, hasta));
        assertThrows(IllegalArgumentException.class, () ->
                servicio.consultarPatronesDemanda(usuario, productoId, List.of(1L, -2L), desde, hasta));
        verifyNoInteractions(productos, tiendas, ventas);
    }

    @Test
    void rechazaRangoDeFechasInvalidoOLocalDateMax() {
        assertThrows(IllegalArgumentException.class, () ->
                servicio.consultarPatronesDemanda(usuario, productoId, List.of(1L), null, hasta));
        assertThrows(IllegalArgumentException.class, () ->
                servicio.consultarPatronesDemanda(usuario, productoId, List.of(1L), desde, null));
        assertThrows(IllegalArgumentException.class, () ->
                servicio.consultarPatronesDemanda(usuario, productoId, List.of(1L), hasta, desde));
        assertThrows(IllegalArgumentException.class, () ->
                servicio.consultarPatronesDemanda(usuario, productoId, List.of(1L), desde, LocalDate.MAX));
        verifyNoInteractions(productos, tiendas, ventas);
    }

    @Test
    void lanzaRecursoNoEncontradoCuandoProductoNoExiste() {
        when(productos.findById(productoId)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () ->
                servicio.consultarPatronesDemanda(usuario, productoId, List.of(1L), desde, hasta));
        verifyNoInteractions(tiendas, ventas);
    }

    @Test
    void lanzaRecursoNoEncontradoCuandoAlgunaTiendaNoExiste() {
        when(productos.findById(productoId)).thenReturn(Optional.of(mock(Producto.class)));
        when(tiendas.obtenerPorId(1L)).thenReturn(Optional.of(mock(Tienda.class)));
        when(tiendas.obtenerPorId(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () ->
                servicio.consultarPatronesDemanda(usuario, productoId, List.of(1L, 99L), desde, hasta));
        verifyNoInteractions(ventas);
    }

    @Test
    void rechazaAccesoCuandoUsuarioNoEsPlanificador() {
        doThrow(new AccesoDenegadoException("Solo planificadores"))
                .when(autorizacion)
                .requirePlanificador(usuario, AuthorizedAction.CONSULTAR_PATRONES_DEMANDA);

        assertThrows(AccesoDenegadoException.class, () ->
                servicio.consultarPatronesDemanda(usuario, productoId, List.of(1L), desde, hasta));
        verifyNoInteractions(productos, tiendas, ventas);
    }

    private PatronesDemandaResponse consultar(List<Long> tiendaIds, List<Venta> ventasDePrueba) {
        when(productos.findById(productoId)).thenReturn(Optional.of(mock(Producto.class)));
        for (Long tiendaId : tiendaIds) {
            when(tiendas.obtenerPorId(tiendaId)).thenReturn(Optional.of(mock(Tienda.class)));
        }

        when(ventas.buscarVentasPorProductoYTiendasEnPeriodo(
                productoId, tiendaIds, desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay()))
                .thenReturn(ventasDePrueba);

        return servicio.consultarPatronesDemanda(usuario, productoId, tiendaIds, desde, hasta);
    }

    private Venta venta(Long tiendaId, LocalDate fecha, int cantidad) {
        return new Venta(null, tiendaId, productoId, cantidad, fecha.atTime(12, 0));
    }
}