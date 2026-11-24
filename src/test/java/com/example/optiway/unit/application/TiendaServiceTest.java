package com.example.optiway;

import com.example.optiway.application.exception.RecursoNoEncontradoException;
import com.example.optiway.application.port.in.CrearTiendaCommand;
import com.example.optiway.application.port.in.dto.TiendaResponse;
import com.example.optiway.application.port.out.AuthorizationPort;
import com.example.optiway.application.port.out.OrdenReposicionRepositoryPort;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.application.service.TiendaService;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.Tienda;
import com.example.optiway.domain.model.Usuario;
import com.example.optiway.infrastructure.adapter.in.rest.dto.CambiarEstadoTiendaRequest;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TiendaServiceTest {

    @Mock private TiendaRepositoryPort tiendaRepositoryPort;
    @Mock private UsuarioRepositoryPort usuarioRepositoryPort;
    @Mock private AuthorizationPort authorizationPort;
    @Mock private OrdenReposicionRepositoryPort ordenReposicionRepositoryPort;

    private TiendaService service;

    private final UUID authUserId = UUID.randomUUID();
    private final Long tiendaId = 10L;

    @BeforeEach
    void setUp() {
        service = new TiendaService(
                tiendaRepositoryPort, usuarioRepositoryPort, authorizationPort, ordenReposicionRepositoryPort);
    }

    @Test
    void crearTienda_CuandoDatosSonValidos_DebeGenerarCodigoYGuardarActiva() {
        when(tiendaRepositoryPort.save(any(Tienda.class))).thenAnswer(inv -> {
            Tienda t = inv.getArgument(0);
            t.setId(tiendaId);
            return t;
        });

        TiendaResponse res = service.crearTienda(
                authUserId, new CrearTiendaCommand("  Tienda Centro  ", "  Cra 10  ", "  Bogota  ", 5L));

        assertEquals(tiendaId, res.id());
        assertEquals("Tienda Centro", res.nombre());
        assertEquals("Cra 10", res.direccion());
        assertEquals("Bogota", res.ciudad());
        assertTrue(res.codigo().startsWith("TND-"));
        assertTrue(res.activo());
        verify(authorizationPort).requireGestionLogistica(authUserId, AuthorizedAction.CREAR_TIENDA);
    }

    @Test
    void obtenerTiendas_SegunRol_DebeFiltrarCorrectamente() {
        Usuario admin = new Usuario(1L, authUserId, "Admin", "a@opt.com", Rol.ADMINISTRADOR, null);
        Usuario encTienda = new Usuario(2L, authUserId, "Enc", "e@opt.com", Rol.ENCARGADO_TIENDA, null);
        Usuario encCd = new Usuario(3L, authUserId, "CD", "c@opt.com", Rol.ENCARGADO_CD, null);
        Tienda t1 = new Tienda(1L, "TND-1", "T1", "Dir", "Med", true, 2L);

        when(authorizationPort.requireAuthenticated(authUserId)).thenReturn(admin);
        when(tiendaRepositoryPort.findAll()).thenReturn(List.of(t1));
        assertEquals(1, service.obtenerTiendas(authUserId).size());

        when(authorizationPort.requireAuthenticated(authUserId)).thenReturn(encTienda);
        when(tiendaRepositoryPort.obtenerPorEncargadoId(2L)).thenReturn(List.of(t1));
        assertEquals(1, service.obtenerTiendas(authUserId).size());

        when(authorizationPort.requireAuthenticated(authUserId)).thenReturn(encCd);
        assertTrue(service.obtenerTiendas(authUserId).isEmpty());
    }

    @Test
    void asignarEncargado_CuandoTiendaEstaInactiva_DebeLanzar409() {
        Tienda inactiva = new Tienda(tiendaId, "TND-10", "T10", "Dir", "Bog", false, null);
        when(tiendaRepositoryPort.obtenerPorId(tiendaId)).thenReturn(Optional.of(inactiva));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                service.asignarEncargado(authUserId, tiendaId, 2L));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(tiendaRepositoryPort, never()).save(any());
    }

    @Test
    void asignarEncargado_CuandoEncargadoTieneRolInvalido_DebeLanzarExcepcion() {
        Tienda activa = new Tienda(tiendaId, "TND-10", "T10", "Dir", "Bog", true, null);
        Usuario planificador = new Usuario(2L, UUID.randomUUID(), "Plan", "p@opt.com", Rol.PLANIFICADOR, null);
        Usuario encCd = new Usuario(3L, UUID.randomUUID(), "CD", "c@opt.com", Rol.ENCARGADO_CD, null);

        when(tiendaRepositoryPort.obtenerPorId(tiendaId)).thenReturn(Optional.of(activa));
        when(usuarioRepositoryPort.obtenerPorId(2L)).thenReturn(Optional.of(planificador));
        when(usuarioRepositoryPort.obtenerPorId(3L)).thenReturn(Optional.of(encCd));

        assertThrows(IllegalArgumentException.class, () -> service.asignarEncargado(authUserId, tiendaId, 2L));
        assertThrows(IllegalArgumentException.class, () -> service.asignarEncargado(authUserId, tiendaId, 3L));
        verify(tiendaRepositoryPort, never()).save(any());
    }

    @Test
    void asignarEncargado_CuandoDatosSonValidos_DebeActualizarEncargado() {
        Tienda activa = new Tienda(tiendaId, "TND-10", "T10", "Dir", "Bog", true, null);
        Usuario encTienda = new Usuario(2L, UUID.randomUUID(), "Enc", "e@opt.com", Rol.ENCARGADO_TIENDA, null);

        when(tiendaRepositoryPort.obtenerPorId(tiendaId)).thenReturn(Optional.of(activa));
        when(usuarioRepositoryPort.obtenerPorId(2L)).thenReturn(Optional.of(encTienda));
        when(tiendaRepositoryPort.save(any(Tienda.class))).thenAnswer(inv -> inv.getArgument(0));

        TiendaResponse res = service.asignarEncargado(authUserId, tiendaId, 2L);
        assertEquals(2L, res.encargadoId());
    }

    @Test
    void cambiarEstado_DesactivarConOrdenesEnTransitoSinConfirmar_DebeLanzar409() {
        Tienda activa = new Tienda(tiendaId, "TND-10", "T10", "Dir", "Bog", true, null);
        when(tiendaRepositoryPort.obtenerPorId(tiendaId)).thenReturn(Optional.of(activa));
        when(ordenReposicionRepositoryPort.existeOrdenEnTransitoOEnviadaPorTienda(tiendaId)).thenReturn(true);

        CambiarEstadoTiendaRequest req = new CambiarEstadoTiendaRequest();
        req.setActivo(false);
        req.setConfirmarConOrdenesEnTransito(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                service.cambiarEstado(authUserId, tiendaId, req));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(tiendaRepositoryPort, never()).save(any());
    }

    @Test
    void cambiarEstado_DesactivarConOrdenesEnTransitoConfirmado_DebeDesactivar() {
        Tienda activa = new Tienda(tiendaId, "TND-10", "T10", "Dir", "Bog", true, null);
        when(tiendaRepositoryPort.obtenerPorId(tiendaId)).thenReturn(Optional.of(activa));
        when(ordenReposicionRepositoryPort.existeOrdenEnTransitoOEnviadaPorTienda(tiendaId)).thenReturn(true);
        when(tiendaRepositoryPort.save(any(Tienda.class))).thenAnswer(inv -> inv.getArgument(0));

        CambiarEstadoTiendaRequest req = new CambiarEstadoTiendaRequest();
        req.setActivo(false);
        req.setConfirmarConOrdenesEnTransito(true);

        TiendaResponse res = service.cambiarEstado(authUserId, tiendaId, req);
        assertNotNull(res);
        assertFalse(res.activo());
    }

    @Test
    void cambiarEstado_CuandoTiendaNoExiste_DebeLanzar404() {
        when(tiendaRepositoryPort.obtenerPorId(tiendaId)).thenReturn(Optional.empty());
        CambiarEstadoTiendaRequest req = new CambiarEstadoTiendaRequest();
        req.setActivo(false);

        assertThrows(RecursoNoEncontradoException.class, () ->
                service.cambiarEstado(authUserId, tiendaId, req));
    }
}
