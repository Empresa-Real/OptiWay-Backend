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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.optiway.application.exception.RecursoNoEncontradoException;
import com.example.optiway.application.exception.StockInsuficienteException;
import com.example.optiway.application.port.in.AprobarRecomendacionCommand;
import com.example.optiway.application.port.in.CrearRecomendacionReposicionCommand;
import com.example.optiway.application.port.in.RechazarRecomendacionCommand;
import com.example.optiway.application.port.in.dto.OrdenReposicionResponse;
import com.example.optiway.application.port.in.dto.RecomendacionReposicionResponse;
import com.example.optiway.application.port.out.AuthorizationPort;
import com.example.optiway.application.port.out.CentroDistribucionRepositoryPort;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.OrdenReposicionRepositoryPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.RecomendacionReposicionRepositoryPort;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.application.service.RecomendacionReposicionService;
import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.domain.model.Inventario;
import com.example.optiway.domain.model.OrdenReposicion;
import com.example.optiway.domain.model.Producto;
import com.example.optiway.domain.model.RecomendacionReposicion;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.Tienda;
import com.example.optiway.domain.model.Usuario;
import com.example.optiway.infrastructure.security.AuthorizedAction;

@ExtendWith(MockitoExtension.class)
class RecomendacionReposicionServiceTest {

    @Mock
    private AuthorizationPort authorizationPort;

    @Mock
    private ProductoRepositoryPort productoRepositoryPort;

    @Mock
    private CentroDistribucionRepositoryPort centroRepositoryPort;

    @Mock
    private TiendaRepositoryPort tiendaRepositoryPort;

    @Mock
    private InventarioRepositoryPort inventarioRepositoryPort;

    @Mock
    private RecomendacionReposicionRepositoryPort recomendacionRepositoryPort;

    @Mock
    private OrdenReposicionRepositoryPort ordenRepositoryPort;

    private RecomendacionReposicionService service;

    private final UUID authUserId = UUID.randomUUID();

    private final Long productoId = 10L;
    private final Long centroId = 20L;
    private final Long tiendaId = 30L;

    private Usuario planificador;
    private Producto producto;
    private CentroDistribucion centro;
    private Tienda tienda;
    private Inventario inventario;

    @BeforeEach
    void setUp() {
        service = new RecomendacionReposicionService(
                authorizationPort,
                productoRepositoryPort,
                centroRepositoryPort,
                tiendaRepositoryPort,
                inventarioRepositoryPort,
                recomendacionRepositoryPort,
                ordenRepositoryPort
        );

        planificador = new Usuario(
                100L, authUserId, "Planificador",
                "planificador@optiway.com", Rol.PLANIFICADOR, null
        );

        producto = new Producto(
                productoId, "PROD-010", "Arroz Diana",
                "Granos", new BigDecimal("4500"), "Unidad", true
        );

        centro = new CentroDistribucion(
                centroId, "CD-020", "Centro Norte",
                "Calle 20", 5000, 200L, List.of(tiendaId)
        );

        tienda = new Tienda(
                tiendaId, "TND-030", "Tienda Norte",
                "Calle 30", "Medellin", true, 300L
        );

        inventario = new Inventario(
                1L, null, centroId, productoId, 100, 10
        );
    }

    private void prepararDatos(boolean dentroDeCobertura, int stock) {
        if (!dentroDeCobertura) {
            centro.setTiendasAbastecidasIds(List.of(999L));
        }

        inventario.setCantidadActual(stock);

        when(authorizationPort.requirePlanificador(
                authUserId, AuthorizedAction.CREAR_RECOMENDACION_REPOSICION
        )).thenReturn(planificador);

        when(productoRepositoryPort.findById(productoId))
                .thenReturn(Optional.of(producto));

        when(centroRepositoryPort.obtenerPorId(centroId))
                .thenReturn(Optional.of(centro));

        when(tiendaRepositoryPort.obtenerPorId(tiendaId))
                .thenReturn(Optional.of(tienda));

        when(inventarioRepositoryPort.findByCentroDistribucionIdAndProductoId(
                centroId, productoId
        )).thenReturn(Optional.of(inventario));
    }

    @Test
    void crearRecomendacion_CuandoDatosSonValidos_DebeGuardarPropuesta() {
        prepararDatos(true, 100);

        CrearRecomendacionReposicionCommand command =
                new CrearRecomendacionReposicionCommand(
                        productoId, 25, centroId, tiendaId, false
                );

        when(recomendacionRepositoryPort.guardar(any(RecomendacionReposicion.class)))
                .thenAnswer(invocation -> {
                    RecomendacionReposicion recomendacion = invocation.getArgument(0);
                    recomendacion.setId(1L);
                    return recomendacion;
                });

        RecomendacionReposicionResponse resultado =
                service.crear(authUserId, command);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals(productoId, resultado.productoId());
        assertEquals("Arroz Diana", resultado.nombreProducto());
        assertEquals(25, resultado.cantidad());
        assertEquals(centroId, resultado.centroDistribucionOrigenId());
        assertEquals(tiendaId, resultado.tiendaDestinoId());
        assertEquals("PROPUESTA", resultado.estado());
        assertFalse(resultado.fueraDeCobertura());
        assertEquals(authUserId, resultado.creadoPor());
        assertNotNull(resultado.fechaCreacion());

        verify(recomendacionRepositoryPort).guardar(any(RecomendacionReposicion.class));

        // Crear una propuesta no debe descontar inventario.
        verify(inventarioRepositoryPort, never()).save(any(Inventario.class));
    }

    @Test
    void crearRecomendacion_CuandoStockEsInsuficiente_DebeLanzarExcepcionYSinGuardar() {
        prepararDatos(true, 10);

        CrearRecomendacionReposicionCommand command =
                new CrearRecomendacionReposicionCommand(
                        productoId, 25, centroId, tiendaId, false
                );

        assertThrows(
                StockInsuficienteException.class,
                () -> service.crear(authUserId, command)
        );

        verify(recomendacionRepositoryPort, never())
                .guardar(any(RecomendacionReposicion.class));

        verify(inventarioRepositoryPort, never()).save(any(Inventario.class));
    }

    @Test
    void crearRecomendacion_FueraDeCoberturaSinConfirmacion_DebeLanzarConflicto() {
        prepararDatos(false, 100);

        CrearRecomendacionReposicionCommand command =
                new CrearRecomendacionReposicionCommand(
                        productoId, 25, centroId, tiendaId, false
                );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.crear(authUserId, command)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(recomendacionRepositoryPort, never())
                .guardar(any(RecomendacionReposicion.class));
    }

    @Test
    void crearRecomendacion_FueraDeCoberturaConConfirmacion_DebeGuardarPropuesta() {
        prepararDatos(false, 100);

        CrearRecomendacionReposicionCommand command =
                new CrearRecomendacionReposicionCommand(
                        productoId, 25, centroId, tiendaId, true
                );

        when(recomendacionRepositoryPort.guardar(any(RecomendacionReposicion.class)))
                .thenAnswer(invocation -> {
                    RecomendacionReposicion recomendacion = invocation.getArgument(0);
                    recomendacion.setId(2L);
                    return recomendacion;
                });

        RecomendacionReposicionResponse resultado =
                service.crear(authUserId, command);

        assertNotNull(resultado);
        assertEquals("PROPUESTA", resultado.estado());
        assertTrue(resultado.fueraDeCobertura());

        verify(recomendacionRepositoryPort).guardar(any(RecomendacionReposicion.class));
    }

    @Test
    void aprobarRecomendacion_SinAjustes_DebeAprobarYCrearOrden() {
        RecomendacionReposicion propuesta = crearPropuestaBase(1L, "PROPUESTA");
        prepararEntidadesActivasParaAprobacion(centroId, centro, inventario);

        when(authorizationPort.requirePlanificador(authUserId, AuthorizedAction.APROBAR_RECOMENDACION_REPOSICION))
                .thenReturn(planificador);
        when(recomendacionRepositoryPort.obtenerPorIdParaActualizar(1L)).thenReturn(Optional.of(propuesta));
        when(recomendacionRepositoryPort.guardar(any(RecomendacionReposicion.class))).thenAnswer(i -> i.getArgument(0));
        when(ordenRepositoryPort.guardar(any(OrdenReposicion.class))).thenAnswer(i -> {
            OrdenReposicion o = i.getArgument(0);
            o.setId(99L);
            return o;
        });

        OrdenReposicionResponse orden = service.aprobar(authUserId, new AprobarRecomendacionCommand(1L, null, null, null));

        assertEquals(99L, orden.id());
        assertEquals(1L, orden.recomendacionId());
        assertEquals(25, orden.cantidad());
        assertEquals("APROBADA", orden.estado());
        assertEquals("APROBADA", propuesta.getEstado());
        assertEquals("APROBADA", propuesta.getAccionTomada());
        assertEquals(authUserId, propuesta.getResueltoPorUsuarioId());
        assertNotNull(propuesta.getFechaResolucion());
    }

    @Test
    void aprobarRecomendacion_SinAjustesCuandoStockBajo_DebeLanzarExcepcionYSinCrearOrden() {
        RecomendacionReposicion propuesta = crearPropuestaBase(10L, "PROPUESTA");
        inventario.setCantidadActual(10);
        prepararEntidadesActivasParaAprobacion(centroId, centro, inventario);

        when(authorizationPort.requirePlanificador(authUserId, AuthorizedAction.APROBAR_RECOMENDACION_REPOSICION))
                .thenReturn(planificador);
        when(recomendacionRepositoryPort.obtenerPorIdParaActualizar(10L)).thenReturn(Optional.of(propuesta));

        assertThrows(StockInsuficienteException.class, () ->
                service.aprobar(authUserId, new AprobarRecomendacionCommand(10L, null, null, null)));
        verify(ordenRepositoryPort, never()).guardar(any());
        verify(recomendacionRepositoryPort, never()).guardar(any());
    }

    @Test
    void aprobarRecomendacion_CuandoProductoOTiendaEstaInactivo_DebeLanzarExcepcion() {
        RecomendacionReposicion propuesta = crearPropuestaBase(11L, "PROPUESTA");
        producto.setActivo(false);

        when(authorizationPort.requirePlanificador(authUserId, AuthorizedAction.APROBAR_RECOMENDACION_REPOSICION))
                .thenReturn(planificador);
        when(recomendacionRepositoryPort.obtenerPorIdParaActualizar(11L)).thenReturn(Optional.of(propuesta));
        when(productoRepositoryPort.findById(productoId)).thenReturn(Optional.of(producto));

        assertThrows(IllegalArgumentException.class, () ->
                service.aprobar(authUserId, new AprobarRecomendacionCommand(11L, null, null, null)));
        verify(ordenRepositoryPort, never()).guardar(any());
    }

    @Test
    void aprobarRecomendacion_ConAjusteDeCantidadYCD_DebeRevalidarStockYCrearOrdenAjustada() {
        Long nuevoCdId = 25L;
        CentroDistribucion nuevoCd = new CentroDistribucion(nuevoCdId, "CD-025", "CD Sur", "Calle 5", 3000, 201L, List.of(tiendaId));
        Inventario inventarioNuevoCd = new Inventario(2L, null, nuevoCdId, productoId, 60, 5);
        RecomendacionReposicion propuesta = crearPropuestaBase(2L, "PROPUESTA");
        prepararEntidadesActivasParaAprobacion(nuevoCdId, nuevoCd, inventarioNuevoCd);

        when(authorizationPort.requirePlanificador(authUserId, AuthorizedAction.APROBAR_RECOMENDACION_REPOSICION))
                .thenReturn(planificador);
        when(recomendacionRepositoryPort.obtenerPorIdParaActualizar(2L)).thenReturn(Optional.of(propuesta));
        when(recomendacionRepositoryPort.guardar(any(RecomendacionReposicion.class))).thenAnswer(i -> i.getArgument(0));
        when(ordenRepositoryPort.guardar(any(OrdenReposicion.class))).thenAnswer(i -> {
            OrdenReposicion o = i.getArgument(0);
            o.setId(100L);
            return o;
        });

        OrdenReposicionResponse orden = service.aprobar(authUserId, new AprobarRecomendacionCommand(2L, 40, nuevoCdId, false));

        assertEquals(40, orden.cantidad());
        assertEquals(nuevoCdId, orden.centroDistribucionOrigenId());
        assertEquals("AJUSTADA_Y_APROBADA", propuesta.getAccionTomada());
        assertEquals(25, propuesta.getCantidadPropuesta());
        assertEquals(centroId, propuesta.getCentroDistribucionPropuestoId());
        assertFalse(propuesta.isFueraDeCobertura());
    }

    @Test
    void aprobarRecomendacion_NuevoCDFueraDeCoberturaSinConfirmar_DebeLanzarConflicto409() {
        Long nuevoCdId = 26L;
        CentroDistribucion nuevoCd = new CentroDistribucion(nuevoCdId, "CD-026", "CD Este", "Calle 9", 3000, 202L, List.of(999L));
        Inventario inventarioNuevoCd = new Inventario(3L, null, nuevoCdId, productoId, 60, 5);
        RecomendacionReposicion propuesta = crearPropuestaBase(12L, "PROPUESTA");
        prepararEntidadesActivasParaAprobacion(nuevoCdId, nuevoCd, inventarioNuevoCd);

        when(authorizationPort.requirePlanificador(authUserId, AuthorizedAction.APROBAR_RECOMENDACION_REPOSICION))
                .thenReturn(planificador);
        when(recomendacionRepositoryPort.obtenerPorIdParaActualizar(12L)).thenReturn(Optional.of(propuesta));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                service.aprobar(authUserId, new AprobarRecomendacionCommand(12L, null, nuevoCdId, false)));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(ordenRepositoryPort, never()).guardar(any());
        verify(recomendacionRepositoryPort, never()).guardar(any());
    }

    @Test
    void aprobarRecomendacion_CuandoStockAjustadoEsInsuficiente_DebeLanzarExcepcionYSinCrearOrden() {
        RecomendacionReposicion propuesta = crearPropuestaBase(3L, "PROPUESTA");
        inventario.setCantidadActual(10);
        prepararEntidadesActivasParaAprobacion(centroId, centro, inventario);

        when(authorizationPort.requirePlanificador(authUserId, AuthorizedAction.APROBAR_RECOMENDACION_REPOSICION))
                .thenReturn(planificador);
        when(recomendacionRepositoryPort.obtenerPorIdParaActualizar(3L)).thenReturn(Optional.of(propuesta));

        assertThrows(StockInsuficienteException.class, () ->
                service.aprobar(authUserId, new AprobarRecomendacionCommand(3L, 50, null, null)));
        verify(ordenRepositoryPort, never()).guardar(any());
    }

    @Test
    void aprobarRecomendacion_CuandoNoEstaEnPropuesta_DebeLanzarConflicto409() {
        RecomendacionReposicion aprobada = crearPropuestaBase(4L, "APROBADA");

        when(authorizationPort.requirePlanificador(authUserId, AuthorizedAction.APROBAR_RECOMENDACION_REPOSICION))
                .thenReturn(planificador);
        when(recomendacionRepositoryPort.obtenerPorIdParaActualizar(4L)).thenReturn(Optional.of(aprobada));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                service.aprobar(authUserId, new AprobarRecomendacionCommand(4L, null, null, null)));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(ordenRepositoryPort, never()).guardar(any());
    }

    @Test
    void aprobarRecomendacion_CuandoNoExiste_DebeLanzar404() {
        when(authorizationPort.requirePlanificador(authUserId, AuthorizedAction.APROBAR_RECOMENDACION_REPOSICION))
                .thenReturn(planificador);
        when(recomendacionRepositoryPort.obtenerPorIdParaActualizar(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () ->
                service.aprobar(authUserId, new AprobarRecomendacionCommand(999L, null, null, null)));
    }

    @Test
    void rechazarRecomendacion_ConMotivoValido_DebeGuardarMotivoYSinCrearOrden() {
        RecomendacionReposicion propuesta = crearPropuestaBase(5L, "PROPUESTA");

        when(authorizationPort.requirePlanificador(authUserId, AuthorizedAction.RECHAZAR_RECOMENDACION_REPOSICION))
                .thenReturn(planificador);
        when(recomendacionRepositoryPort.obtenerPorIdParaActualizar(5L)).thenReturn(Optional.of(propuesta));
        when(recomendacionRepositoryPort.guardar(any(RecomendacionReposicion.class))).thenAnswer(i -> i.getArgument(0));

        RecomendacionReposicionResponse res = service.rechazar(
                authUserId, new RechazarRecomendacionCommand(5L, "  Sobre-stock en tienda  "));

        assertEquals("RECHAZADA", res.estado());
        assertEquals("Sobre-stock en tienda", res.motivoRechazo());
        assertEquals("RECHAZADA", res.accionTomada());
        assertEquals(authUserId, res.resueltoPorUsuarioId());
        assertNotNull(res.fechaResolucion());
        verify(ordenRepositoryPort, never()).guardar(any());
    }

    @Test
    void rechazarRecomendacion_SinMotivoOMuyLargo_DebeLanzar400() {
        when(authorizationPort.requirePlanificador(authUserId, AuthorizedAction.RECHAZAR_RECOMENDACION_REPOSICION))
                .thenReturn(planificador);

        assertThrows(IllegalArgumentException.class, () ->
                service.rechazar(authUserId, new RechazarRecomendacionCommand(5L, "   ")));
        assertThrows(IllegalArgumentException.class, () ->
                service.rechazar(authUserId, new RechazarRecomendacionCommand(5L, "a".repeat(501))));
        verify(recomendacionRepositoryPort, never()).guardar(any());
    }

    private void prepararEntidadesActivasParaAprobacion(
            Long cdObjetivoId, CentroDistribucion cdObjetivo, Inventario invObjetivo) {
        when(productoRepositoryPort.findById(productoId)).thenReturn(Optional.of(producto));
        when(tiendaRepositoryPort.obtenerPorId(tiendaId)).thenReturn(Optional.of(tienda));
        when(centroRepositoryPort.obtenerPorId(cdObjetivoId)).thenReturn(Optional.of(cdObjetivo));
        when(inventarioRepositoryPort.findByCentroDistribucionIdAndProductoId(cdObjetivoId, productoId))
                .thenReturn(Optional.of(invObjetivo));
    }

    private RecomendacionReposicion crearPropuestaBase(Long id, String estado) {
        RecomendacionReposicion rec = new RecomendacionReposicion();
        rec.setId(id);
        rec.setProductoId(productoId);
        rec.setNombreProducto("Arroz Diana");
        rec.setCantidad(25);
        rec.setCentroDistribucionOrigenId(centroId);
        rec.setTiendaDestinoId(tiendaId);
        rec.setEstado(estado);
        rec.setFueraDeCobertura(false);
        rec.setCreadoPor(authUserId);
        return rec;
    }
}
