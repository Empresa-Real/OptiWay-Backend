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

import com.example.optiway.application.exception.StockInsuficienteException;
import com.example.optiway.application.port.in.CrearRecomendacionReposicionCommand;
import com.example.optiway.application.port.in.dto.RecomendacionReposicionResponse;
import com.example.optiway.application.port.out.AuthorizationPort;
import com.example.optiway.application.port.out.CentroDistribucionRepositoryPort;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.RecomendacionReposicionRepositoryPort;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.application.service.RecomendacionReposicionService;
import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.domain.model.Inventario;
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
                recomendacionRepositoryPort
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
}
