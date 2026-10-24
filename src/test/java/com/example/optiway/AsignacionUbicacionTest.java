package com.example.optiway.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.optiway.application.exception.RecursoNoEncontradoException;
import com.example.optiway.application.port.in.dto.CentroDistribucionResponse;
import com.example.optiway.application.port.in.dto.TiendaResponse;
import com.example.optiway.application.port.out.CentroDistribucionRepositoryPort;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.Tienda;
import com.example.optiway.domain.model.Usuario;
import com.example.optiway.infrastructure.security.AuthorizationService;

@ExtendWith(MockitoExtension.class)
class AsignacionUbicacionTest {

    @Mock
    private TiendaRepositoryPort tiendaRepositoryPort;

    @Mock
    private CentroDistribucionRepositoryPort centroDistribucionRepositoryPort;

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    private TiendaService tiendaService;
    private CentroDistribucionService centroDistribucionService;

    private UUID adminAuthId;
    private Usuario adminUsuario;

    @BeforeEach
    void setUp() {
        AuthorizationService authService = new AuthorizationService(usuarioRepositoryPort);
        tiendaService = new TiendaService(tiendaRepositoryPort, usuarioRepositoryPort, authService);
        centroDistribucionService = new CentroDistribucionService(centroDistribucionRepositoryPort, usuarioRepositoryPort, authService);

        adminAuthId = UUID.randomUUID();
        adminUsuario = new Usuario(1L, null, "Admin", "admin@optiway.com", Rol.ADMINISTRADOR, null);
    }

    @Test
    void asignarTienda_conEncargadoTienda_exitoso() {
        Tienda tienda = new Tienda(10L, "TND-001", "Tienda Central", "Calle 1", "Medellin", true, null);
        Usuario encargado = new Usuario(2L, null, "Encargado TND", "tnd@optiway.com", Rol.ENCARGADO_TIENDA, null);

        when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(adminUsuario));
        when(tiendaRepositoryPort.obtenerPorId(10L)).thenReturn(Optional.of(tienda));
        when(usuarioRepositoryPort.obtenerPorId(2L)).thenReturn(Optional.of(encargado));
        when(tiendaRepositoryPort.save(any(Tienda.class))).thenAnswer(inv -> inv.getArgument(0));

        TiendaResponse resultado = tiendaService.asignarEncargado(adminAuthId, 10L, 2L);

        assertEquals(2L, resultado.encargadoId());
        verify(tiendaRepositoryPort).save(tienda);
    }

    @Test
    void asignarTienda_conEncargadoCD_lanzaErrorRolInvalido() {
        Tienda tienda = new Tienda(10L, "TND-001", "Tienda Central", "Calle 1", "Medellin", true, null);
        Usuario encargadoCd = new Usuario(3L, null, "Encargado CD", "cd@optiway.com", Rol.ENCARGADO_CD, null);

        when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(adminUsuario));
        when(tiendaRepositoryPort.obtenerPorId(10L)).thenReturn(Optional.of(tienda));
        when(usuarioRepositoryPort.obtenerPorId(3L)).thenReturn(Optional.of(encargadoCd));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                tiendaService.asignarEncargado(adminAuthId, 10L, 3L));

        assertEquals("Solo se puede asignar una tienda a un usuario con rol Encargado de tienda (Rol actual: ENCARGADO_CD).", ex.getMessage());
    }

    @Test
    void asignarCD_conEncargadoCD_exitoso() {
        CentroDistribucion cd = new CentroDistribucion(20L, "CD-001", "CD Norte", "Autopista 2", 5000, null, null);
        Usuario encargadoCd = new Usuario(3L, null, "Encargado CD", "cd@optiway.com", Rol.ENCARGADO_CD, null);

        when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(adminUsuario));
        when(centroDistribucionRepositoryPort.obtenerPorId(20L)).thenReturn(Optional.of(cd));
        when(usuarioRepositoryPort.obtenerPorId(3L)).thenReturn(Optional.of(encargadoCd));
        when(centroDistribucionRepositoryPort.guardar(any(CentroDistribucion.class))).thenAnswer(inv -> inv.getArgument(0));

        CentroDistribucionResponse resultado = centroDistribucionService.asignarEncargado(adminAuthId, 20L, 3L);

        assertEquals(3L, resultado.encargadoId());
        verify(centroDistribucionRepositoryPort).guardar(cd);
    }

    @Test
    void asignarCD_conEncargadoTienda_lanzaErrorRolInvalido() {
        CentroDistribucion cd = new CentroDistribucion(20L, "CD-001", "CD Norte", "Autopista 2", 5000, null, null);
        Usuario encargadoTnd = new Usuario(2L, null, "Encargado TND", "tnd@optiway.com", Rol.ENCARGADO_TIENDA, null);

        when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(adminUsuario));
        when(centroDistribucionRepositoryPort.obtenerPorId(20L)).thenReturn(Optional.of(cd));
        when(usuarioRepositoryPort.obtenerPorId(2L)).thenReturn(Optional.of(encargadoTnd));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                centroDistribucionService.asignarEncargado(adminAuthId, 20L, 2L));

        assertEquals("Solo se puede asignar un centro de distribución a un usuario con rol Encargado de CD (Rol actual: ENCARGADO_TIENDA).", ex.getMessage());
    }

    @Test
    void asignarTienda_aAdmin_lanzaErrorUbicacionInvalida() {
        Tienda tienda = new Tienda(10L, "TND-001", "Tienda Central", "Calle 1", "Medellin", true, null);
        Usuario otroAdmin = new Usuario(5L, null, "Otro Admin", "admin2@optiway.com", Rol.ADMINISTRADOR, null);

        when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(adminUsuario));
        when(tiendaRepositoryPort.obtenerPorId(10L)).thenReturn(Optional.of(tienda));
        when(usuarioRepositoryPort.obtenerPorId(5L)).thenReturn(Optional.of(otroAdmin));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                tiendaService.asignarEncargado(adminAuthId, 10L, 5L));

        assertEquals("No se puede asignar una ubicación a un usuario Administrador o Planificador (Rol actual: ADMINISTRADOR).", ex.getMessage());
        verify(tiendaRepositoryPort, never()).save(any(Tienda.class));
    }

    @Test
    void asignarTienda_aPlanificador_lanzaErrorUbicacionInvalida() {
        Tienda tienda = new Tienda(10L, "TND-001", "Tienda Central", "Calle 1", "Medellin", true, null);
        Usuario planificador = new Usuario(6L, null, "Planificador", "plan@optiway.com", Rol.PLANIFICADOR, null);

        when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(adminUsuario));
        when(tiendaRepositoryPort.obtenerPorId(10L)).thenReturn(Optional.of(tienda));
        when(usuarioRepositoryPort.obtenerPorId(6L)).thenReturn(Optional.of(planificador));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                tiendaService.asignarEncargado(adminAuthId, 10L, 6L));

        assertEquals("No se puede asignar una ubicación a un usuario Administrador o Planificador (Rol actual: PLANIFICADOR).", ex.getMessage());
        verify(tiendaRepositoryPort, never()).save(any(Tienda.class));
    }

    @Test
    void asignarCD_aPlanificador_lanzaErrorUbicacionInvalida() {
        CentroDistribucion cd = new CentroDistribucion(20L, "CD-001", "CD Norte", "Autopista 2", 5000, null, null);
        Usuario planificador = new Usuario(6L, null, "Planificador", "plan@optiway.com", Rol.PLANIFICADOR, null);

        when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(adminUsuario));
        when(centroDistribucionRepositoryPort.obtenerPorId(20L)).thenReturn(Optional.of(cd));
        when(usuarioRepositoryPort.obtenerPorId(6L)).thenReturn(Optional.of(planificador));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                centroDistribucionService.asignarEncargado(adminAuthId, 20L, 6L));

        assertEquals("No se puede asignar una ubicación a un usuario Administrador o Planificador (Rol actual: PLANIFICADOR).", ex.getMessage());
        verify(centroDistribucionRepositoryPort, never()).guardar(any(CentroDistribucion.class));
    }

    @Test
    void asignarTienda_porPlanificador_exitoso() {
        UUID planificadorAuthId = UUID.randomUUID();
        Usuario planificador = new Usuario(6L, planificadorAuthId, "Planificador", "plan@optiway.com", Rol.PLANIFICADOR, null);
        Tienda tienda = new Tienda(10L, "TND-001", "Tienda Central", "Calle 1", "Medellin", true, null);
        Usuario encargado = new Usuario(2L, null, "Encargado TND", "tnd@optiway.com", Rol.ENCARGADO_TIENDA, null);

        when(usuarioRepositoryPort.obtenerPorAuthUserId(planificadorAuthId)).thenReturn(Optional.of(planificador));
        when(tiendaRepositoryPort.obtenerPorId(10L)).thenReturn(Optional.of(tienda));
        when(usuarioRepositoryPort.obtenerPorId(2L)).thenReturn(Optional.of(encargado));
        when(tiendaRepositoryPort.save(any(Tienda.class))).thenAnswer(inv -> inv.getArgument(0));

        TiendaResponse resultado = tiendaService.asignarEncargado(planificadorAuthId, 10L, 2L);

        assertEquals(2L, resultado.encargadoId());
        verify(tiendaRepositoryPort).save(tienda);
    }

    @Test
    void asignarCD_centroNoExiste_lanzaRecursoNoEncontradoException() {
        when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(adminUsuario));
        when(centroDistribucionRepositoryPort.obtenerPorId(99L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class, () ->
                centroDistribucionService.asignarEncargado(adminAuthId, 99L, 3L));

        assertEquals("Centro de distribución no encontrado con ID: 99", ex.getMessage());
        verify(centroDistribucionRepositoryPort, never()).guardar(any(CentroDistribucion.class));
    }

    @Test
    void asignarCD_encargadoNoExiste_lanzaRecursoNoEncontradoException() {
        CentroDistribucion cd = new CentroDistribucion(20L, "CD-001", "CD Norte", "Autopista 2", 5000, null, null);
        when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(adminUsuario));
        when(centroDistribucionRepositoryPort.obtenerPorId(20L)).thenReturn(Optional.of(cd));
        when(usuarioRepositoryPort.obtenerPorId(999L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class, () ->
                centroDistribucionService.asignarEncargado(adminAuthId, 20L, 999L));

        assertEquals("Usuario encargado no encontrado con ID: 999", ex.getMessage());
        verify(centroDistribucionRepositoryPort, never()).guardar(any(CentroDistribucion.class));
    }

    @Test
    void asignarTienda_tiendaNoExiste_lanzaRecursoNoEncontradoException() {
        when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(adminUsuario));
        when(tiendaRepositoryPort.obtenerPorId(99L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class, () ->
                tiendaService.asignarEncargado(adminAuthId, 99L, 2L));

        assertEquals("Tienda no encontrada con ID: 99", ex.getMessage());
        verify(tiendaRepositoryPort, never()).save(any(Tienda.class));
    }

    @Test
    void asignarTienda_encargadoNoExiste_lanzaRecursoNoEncontradoException() {
        Tienda tienda = new Tienda(10L, "TND-001", "Tienda Central", "Calle 1", "Medellin", true, null);
        when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(adminUsuario));
        when(tiendaRepositoryPort.obtenerPorId(10L)).thenReturn(Optional.of(tienda));
        when(usuarioRepositoryPort.obtenerPorId(999L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class, () ->
                tiendaService.asignarEncargado(adminAuthId, 10L, 999L));

        assertEquals("Usuario encargado no encontrado con ID: 999", ex.getMessage());
        verify(tiendaRepositoryPort, never()).save(any(Tienda.class));
    }
}
