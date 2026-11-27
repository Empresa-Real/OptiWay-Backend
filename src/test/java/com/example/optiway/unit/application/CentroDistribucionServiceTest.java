package com.example.optiway.unit.application;

import com.example.optiway.application.exception.RecursoNoEncontradoException;
import com.example.optiway.application.port.in.CrearCentroDistribucionCommand;
import com.example.optiway.application.port.in.dto.CentroDistribucionResponse;
import com.example.optiway.application.port.in.dto.OrdenReposicionResponse;
import com.example.optiway.application.port.out.AuthorizationPort;
import com.example.optiway.application.port.out.CentroDistribucionRepositoryPort;
import com.example.optiway.application.port.out.OrdenReposicionRepositoryPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.application.service.CentroDistribucionService;
import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.domain.model.OrdenReposicion;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.Usuario;
import com.example.optiway.infrastructure.security.AuthorizedAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CentroDistribucionServiceTest {

    @Mock private CentroDistribucionRepositoryPort centroRepositoryPort;
    @Mock private UsuarioRepositoryPort usuarioRepositoryPort;
    @Mock private AuthorizationPort authorizationPort;
    @Mock private OrdenReposicionRepositoryPort ordenReposicionRepositoryPort;

    private CentroDistribucionService service;

    private final UUID authUserId = UUID.randomUUID();
    private final Long cdId = 20L;

    @BeforeEach
    void setUp() {
        service = new CentroDistribucionService(
                centroRepositoryPort, usuarioRepositoryPort, authorizationPort, ordenReposicionRepositoryPort);
    }

    @Test
    void crearCentroDistribucion_CuandoDatosSonValidos_DebeGenerarCodigoYGuardar() {
        when(centroRepositoryPort.guardar(any(CentroDistribucion.class))).thenAnswer(inv -> {
            CentroDistribucion c = inv.getArgument(0);
            c.setId(cdId);
            return c;
        });

        CentroDistribucionResponse res = service.crearCentroDistribucion(
                authUserId, new CrearCentroDistribucionCommand("  CD Norte  ", "  Autopista  ", 1000, 5L, List.of(1L, 2L)));

        assertEquals(cdId, res.id());
        assertEquals("CD Norte", res.nombre());
        assertEquals("Autopista", res.direccion());
        assertTrue(res.codigo().startsWith("CD-"));
        assertEquals(List.of(1L, 2L), res.tiendasAbastecidasIds());
        verify(authorizationPort).requireGestionLogistica(authUserId, AuthorizedAction.CREAR_CENTRO_DISTRIBUCION);
    }

    @Test
    void obtenerCentrosDistribucion_SegunRol_DebeFiltrarCorrectamente() {
        Usuario admin = new Usuario(1L, authUserId, "Admin", "a@opt.com", Rol.ADMINISTRADOR, null);
        Usuario encCd = new Usuario(2L, authUserId, "CD", "c@opt.com", Rol.ENCARGADO_CD, null);
        Usuario encTienda = new Usuario(3L, authUserId, "Enc", "e@opt.com", Rol.ENCARGADO_TIENDA, null);
        CentroDistribucion cd = new CentroDistribucion(cdId, "CD-1", "CD1", "Dir", 500, 2L, List.of());

        when(authorizationPort.requireAuthenticated(authUserId)).thenReturn(admin);
        when(centroRepositoryPort.obtenerTodos()).thenReturn(List.of(cd));
        assertEquals(1, service.obtenerCentrosDistribucion(authUserId).size());

        when(authorizationPort.requireAuthenticated(authUserId)).thenReturn(encCd);
        when(centroRepositoryPort.obtenerPorEncargadoId(2L)).thenReturn(List.of(cd));
        assertEquals(1, service.obtenerCentrosDistribucion(authUserId).size());

        when(authorizationPort.requireAuthenticated(authUserId)).thenReturn(encTienda);
        assertTrue(service.obtenerCentrosDistribucion(authUserId).isEmpty());
    }

    @Test
    void asignarEncargado_CuandoCentroNoExiste_DebeLanzar404() {
        when(centroRepositoryPort.obtenerPorId(cdId)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () ->
                service.asignarEncargado(authUserId, cdId, 2L));
    }

    @Test
    void asignarEncargado_CuandoEncargadoNoEsEncargadoCD_DebeLanzarExcepcion() {
        CentroDistribucion cd = new CentroDistribucion(cdId, "CD-1", "CD1", "Dir", 500, null, List.of());
        Usuario planificador = new Usuario(2L, UUID.randomUUID(), "Plan", "p@opt.com", Rol.PLANIFICADOR, null);
        Usuario encTienda = new Usuario(3L, UUID.randomUUID(), "Enc", "e@opt.com", Rol.ENCARGADO_TIENDA, null);

        when(centroRepositoryPort.obtenerPorId(cdId)).thenReturn(Optional.of(cd));
        when(usuarioRepositoryPort.obtenerPorId(2L)).thenReturn(Optional.of(planificador));
        when(usuarioRepositoryPort.obtenerPorId(3L)).thenReturn(Optional.of(encTienda));

        assertThrows(IllegalArgumentException.class, () -> service.asignarEncargado(authUserId, cdId, 2L));
        assertThrows(IllegalArgumentException.class, () -> service.asignarEncargado(authUserId, cdId, 3L));
        verify(centroRepositoryPort, never()).guardar(any());
    }

    @Test
    void asignarEncargado_CuandoDatosSonValidos_DebeActualizarEncargado() {
        CentroDistribucion cd = new CentroDistribucion(cdId, "CD-1", "CD1", "Dir", 500, null, List.of());
        Usuario encCd = new Usuario(2L, UUID.randomUUID(), "CD", "c@opt.com", Rol.ENCARGADO_CD, null);

        when(centroRepositoryPort.obtenerPorId(cdId)).thenReturn(Optional.of(cd));
        when(usuarioRepositoryPort.obtenerPorId(2L)).thenReturn(Optional.of(encCd));
        when(centroRepositoryPort.guardar(any(CentroDistribucion.class))).thenAnswer(inv -> inv.getArgument(0));

        CentroDistribucionResponse res = service.asignarEncargado(authUserId, cdId, 2L);
        assertEquals(2L, res.encargadoId());
    }

    @Test
    void cambiarEstado_DesactivarConTiendasAsignadasSinConfirmar_DebeLanzar409() {
        CentroDistribucion cd = new CentroDistribucion(cdId, "CD-1", "CD1", "Dir", 500, null, List.of(1L));
        when(centroRepositoryPort.obtenerPorId(cdId)).thenReturn(Optional.of(cd));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                service.cambiarEstado(authUserId, cdId, false, false));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(centroRepositoryPort, never()).guardar(any());
    }

    @Test
    void cambiarEstado_DesactivarConTiendasAsignadasConfirmado_DebeDesactivar() {
        CentroDistribucion cd = new CentroDistribucion(cdId, "CD-1", "CD1", "Dir", 500, null, List.of(1L));
        when(centroRepositoryPort.obtenerPorId(cdId)).thenReturn(Optional.of(cd));
        when(centroRepositoryPort.guardar(any(CentroDistribucion.class))).thenAnswer(inv -> inv.getArgument(0));

        CentroDistribucionResponse res = service.cambiarEstado(authUserId, cdId, false, true);
        assertEquals("Inactivo", res.estado());
    }

    @Test
    void obtenerOrdenesPorCentro_CuandoUsuarioAutorizado_DebeRetornarOrdenes() {
        CentroDistribucion cd = new CentroDistribucion(cdId, "CD-1", "CD1", "Dir", 500, 2L, List.of(1L));
        OrdenReposicion orden = new OrdenReposicion();
        orden.setId(77L);
        orden.setRecomendacionId(15L);
        orden.setProductoId(5L);
        orden.setNombreProducto("Arroz Diana");
        orden.setCantidad(25);
        orden.setCentroDistribucionOrigenId(cdId);
        orden.setTiendaDestinoId(1L);
        orden.setEstado("APROBADA");
        orden.setCreadoPor(authUserId);

        when(centroRepositoryPort.obtenerPorId(cdId)).thenReturn(Optional.of(cd));
        when(ordenReposicionRepositoryPort.listarPorCentroDistribucionOrigen(cdId)).thenReturn(List.of(orden));

        List<OrdenReposicionResponse> res = service.obtenerOrdenesPorCentro(authUserId, cdId);

        assertEquals(1, res.size());
        assertEquals(77L, res.get(0).id());
        assertEquals("Arroz Diana", res.get(0).nombreProducto());
        assertEquals("APROBADA", res.get(0).estado());
        verify(authorizationPort).requirePermisoCentroDistribucion(
                authUserId, cd, AuthorizedAction.LISTAR_ORDENES_CENTRO_DISTRIBUCION);
    }

    @Test
    void obtenerOrdenesPorCentro_CuandoCentroNoExiste_DebeLanzar404() {
        when(centroRepositoryPort.obtenerPorId(cdId)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () ->
                service.obtenerOrdenesPorCentro(authUserId, cdId));
    }

    @Test
    void obtenerOrdenesPorCentro_CuandoIdEsInvalido_DebeLanzar400() {
        assertThrows(IllegalArgumentException.class, () ->
                service.obtenerOrdenesPorCentro(authUserId, 0L));
    }
}
