package com.example.optiway.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.optiway.application.port.out.CentroDistribucionRepositoryPort;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.Tienda;
import com.example.optiway.domain.model.Usuario;

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
        tiendaService = new TiendaService(tiendaRepositoryPort, usuarioRepositoryPort);
        centroDistribucionService = new CentroDistribucionService(centroDistribucionRepositoryPort, usuarioRepositoryPort);

        adminAuthId = UUID.randomUUID();
        adminUsuario = new Usuario(1L, "Admin", "admin@optiway.com", Rol.ADMINISTRADOR, null);
    }

    @Test
    void asignarTienda_conEncargadoTienda_exitoso() {
        Tienda tienda = new Tienda(10L, "TND-001", "Tienda Central", "Calle 1", "Medellin", "Activa", null);
        Usuario encargado = new Usuario(2L, "Encargado TND", "tnd@optiway.com", Rol.ENCARGADO_TIENDA, null);

        when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(adminUsuario));
        when(tiendaRepositoryPort.obtenerPorId(10L)).thenReturn(Optional.of(tienda));
        when(usuarioRepositoryPort.obtenerPorID(2L)).thenReturn(Optional.of(encargado));
        when(tiendaRepositoryPort.save(any(Tienda.class))).thenAnswer(inv -> inv.getArgument(0));

        Tienda resultado = tiendaService.asignarEncargado(10L, 2L, adminAuthId);

        assertEquals(2L, resultado.getEncargadoId());
        verify(tiendaRepositoryPort).save(tienda);
    }

    @Test
    void asignarTienda_conEncargadoCD_lanzaErrorRolInvalido() {
        Tienda tienda = new Tienda(10L, "TND-001", "Tienda Central", "Calle 1", "Medellin", "Activa", null);
        Usuario encargadoCd = new Usuario(3L, "Encargado CD", "cd@optiway.com", Rol.ENCARGADO_CD, null);

        when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(adminUsuario));
        when(tiendaRepositoryPort.obtenerPorId(10L)).thenReturn(Optional.of(tienda));
        when(usuarioRepositoryPort.obtenerPorID(3L)).thenReturn(Optional.of(encargadoCd));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                tiendaService.asignarEncargado(10L, 3L, adminAuthId));

        assertEquals("Solo se puede asignar una tienda a un usuario con rol Encargado de tienda (Rol actual: ENCARGADO_CD).", ex.getMessage());
    }

    @Test
    void asignarCD_conEncargadoCD_exitoso() {
        CentroDistribucion cd = new CentroDistribucion(20L, "CD-001", "CD Norte", "Autopista 2", 5000, null, null);
        Usuario encargadoCd = new Usuario(3L, "Encargado CD", "cd@optiway.com", Rol.ENCARGADO_CD, null);

        when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(adminUsuario));
        when(centroDistribucionRepositoryPort.obtenerPorId(20L)).thenReturn(Optional.of(cd));
        when(usuarioRepositoryPort.obtenerPorID(3L)).thenReturn(Optional.of(encargadoCd));
        when(centroDistribucionRepositoryPort.guardar(any(CentroDistribucion.class))).thenAnswer(inv -> inv.getArgument(0));

        CentroDistribucion resultado = centroDistribucionService.asignarEncargado(20L, 3L, adminAuthId);

        assertEquals(3L, resultado.getEncargadoId());
        verify(centroDistribucionRepositoryPort).guardar(cd);
    }

    @Test
    void asignarCD_conEncargadoTienda_lanzaErrorRolInvalido() {
        CentroDistribucion cd = new CentroDistribucion(20L, "CD-001", "CD Norte", "Autopista 2", 5000, null, null);
        Usuario encargadoTnd = new Usuario(2L, "Encargado TND", "tnd@optiway.com", Rol.ENCARGADO_TIENDA, null);

        when(usuarioRepositoryPort.obtenerPorAuthUserId(adminAuthId)).thenReturn(Optional.of(adminUsuario));
        when(centroDistribucionRepositoryPort.obtenerPorId(20L)).thenReturn(Optional.of(cd));
        when(usuarioRepositoryPort.obtenerPorID(2L)).thenReturn(Optional.of(encargadoTnd));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                centroDistribucionService.asignarEncargado(20L, 2L, adminAuthId));

        assertEquals("Solo se puede asignar un centro de distribución a un usuario con rol Encargado de CD (Rol actual: ENCARGADO_TIENDA).", ex.getMessage());
    }
}
