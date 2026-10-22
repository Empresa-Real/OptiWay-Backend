package com.example.optiway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.application.exception.AccesoDenegadoException;
import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.Tienda;
import com.example.optiway.domain.model.Usuario;
import com.example.optiway.infrastructure.security.AccionAutorizada;
import com.example.optiway.infrastructure.security.AuthorizationService;

/**
 * Pruebas de las guardas de autorización. Cada guarda se cubre en sus cuatro
 * desenlaces posibles: sin identidad, identidad inexistente en el repositorio,
 * identidad con rol sin permiso y éxito. Es el punto único donde se decide
 * "puede / no puede", así que se prueba de forma exhaustiva y aislada.
 */
@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    private static final Long TIENDA_ID = 10L;
    private static final Long CD_ID = 20L;
    private static final Long ENCARGADO_TIENDA_ID = 100L;
    private static final Long ENCARGADO_CD_ID = 200L;
    private static final Long OTRO_ENCARGADO_ID = 999L;

    @Mock
    private UsuarioRepositoryPort usuarioRepositoryPort;

    private AuthorizationService authorizationService;

    private UUID adminAuthId;
    private UUID planificadorAuthId;
    private UUID encargadoTiendaAuthId;
    private UUID encargadoCdAuthId;

    private Usuario admin;
    private Usuario planificador;
    private Usuario encargadoTienda;
    private Usuario encargadoCd;

    @BeforeEach
    void setUp() {
        authorizationService = new AuthorizationService(usuarioRepositoryPort);

        adminAuthId = UUID.randomUUID();
        planificadorAuthId = UUID.randomUUID();
        encargadoTiendaAuthId = UUID.randomUUID();
        encargadoCdAuthId = UUID.randomUUID();

        admin = usuario(1L, adminAuthId, Rol.ADMINISTRADOR);
        planificador = usuario(2L, planificadorAuthId, Rol.PLANIFICADOR);
        encargadoTienda = usuario(ENCARGADO_TIENDA_ID, encargadoTiendaAuthId, Rol.ENCARGADO_TIENDA);
        encargadoCd = usuario(ENCARGADO_CD_ID, encargadoCdAuthId, Rol.ENCARGADO_CD);
    }

    private Usuario usuario(Long id, UUID authId, Rol rol) {
        return new Usuario(id, authId, rol.name(), rol.name().toLowerCase() + "@optiway.com", rol, null);
    }

    private Tienda tienda(Long encargadoId) {
        return new Tienda(TIENDA_ID, "TND-001", "Tienda", "Dir", "Ciudad", true, encargadoId);
    }

    private CentroDistribucion centro(Long encargadoId) {
        return new CentroDistribucion(CD_ID, "CD-001", "CD", "Dir", 5000, encargadoId, null);
    }

    private void autenticar(UUID authId, Usuario usuario) {
        when(usuarioRepositoryPort.obtenerPorAuthUserId(authId)).thenReturn(Optional.of(usuario));
    }

    // ------------------------------------------------------------------
    // requireAuthenticated
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("requireAuthenticated")
    class RequireAuthenticated {

        @Test
        @DisplayName("Sin identidad lanza AccesoDenegadoException")
        void sinIdentidad_lanza() {
            AccesoDenegadoException e = assertThrows(AccesoDenegadoException.class,
                    () -> authorizationService.requireAuthenticated(null));
            assertEquals("Autenticación requerida", e.getMessage());
        }

        @Test
        @DisplayName("Identidad que no existe en el repositorio lanza AccesoDenegadoException")
        void identidadInexistente_lanza() {
            UUID desconocida = UUID.randomUUID();
            when(usuarioRepositoryPort.obtenerPorAuthUserId(desconocida)).thenReturn(Optional.empty());
            AccesoDenegadoException e = assertThrows(AccesoDenegadoException.class,
                    () -> authorizationService.requireAuthenticated(desconocida));
            assertEquals("Usuario no encontrado", e.getMessage());
        }

        @Test
        @DisplayName("Cualquier rol autenticado pasa")
        void cualquierRolAutenticado_pasa() {
            autenticar(encargadoCdAuthId, encargadoCd);
            assertSame(encargadoCd, authorizationService.requireAuthenticated(encargadoCdAuthId));
        }
    }

    // ------------------------------------------------------------------
    // requireAdministrador
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("requireAdministrador")
    class RequireAdministrador {

        @Test
        @DisplayName("Sin identidad lanza y nombra la acción")
        void sinIdentidad_lanza() {
            AccesoDenegadoException e = assertThrows(AccesoDenegadoException.class,
                    () -> authorizationService.requireAdministrador(null, AccionAutorizada.CREAR_USUARIO));
            assertEquals("Autenticación requerida para crear usuarios", e.getMessage());
        }

        @Test
        @DisplayName("Identidad inexistente lanza")
        void identidadInexistente_lanza() {
            UUID desconocida = UUID.randomUUID();
            when(usuarioRepositoryPort.obtenerPorAuthUserId(desconocida)).thenReturn(Optional.empty());
            assertThrows(AccesoDenegadoException.class,
                    () -> authorizationService.requireAdministrador(desconocida, AccionAutorizada.CREAR_USUARIO));
        }

        @Test
        @DisplayName("PLANIFICADOR no es administrador")
        void planificador_noPasa() {
            autenticar(planificadorAuthId, planificador);
            AccesoDenegadoException e = assertThrows(AccesoDenegadoException.class,
                    () -> authorizationService.requireAdministrador(planificadorAuthId, AccionAutorizada.LISTAR_USUARIOS));
            assertEquals("Solo un administrador puede consultar la lista de usuarios", e.getMessage());
        }

        @Test
        @DisplayName("Encargados no son administradores")
        void encargados_noPasan() {
            autenticar(encargadoTiendaAuthId, encargadoTienda);
            autenticar(encargadoCdAuthId, encargadoCd);
            assertThrows(AccesoDenegadoException.class, () -> authorizationService
                    .requireAdministrador(encargadoTiendaAuthId, AccionAutorizada.ELIMINAR_USUARIO));
            assertThrows(AccesoDenegadoException.class, () -> authorizationService
                    .requireAdministrador(encargadoCdAuthId, AccionAutorizada.ACTUALIZAR_ROL));
        }

        @Test
        @DisplayName("ADMINISTRADOR pasa y se devuelve el usuario")
        void admin_pasa() {
            autenticar(adminAuthId, admin);
            assertSame(admin, authorizationService
                    .requireAdministrador(adminAuthId, AccionAutorizada.CREAR_USUARIO));
        }
    }

    // ------------------------------------------------------------------
    // requireGestionLogistica
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("requireGestionLogistica")
    class RequireGestionLogistica {

        @Test
        @DisplayName("Sin identidad lanza")
        void sinIdentidad_lanza() {
            assertThrows(AccesoDenegadoException.class, () -> authorizationService
                    .requireGestionLogistica(null, AccionAutorizada.CREAR_TIENDA));
        }

        @Test
        @DisplayName("ADMINISTRADOR pasa")
        void admin_pasa() {
            autenticar(adminAuthId, admin);
            assertSame(admin, authorizationService
                    .requireGestionLogistica(adminAuthId, AccionAutorizada.CREAR_TIENDA));
        }

        @Test
        @DisplayName("PLANIFICADOR pasa")
        void planificador_pasa() {
            autenticar(planificadorAuthId, planificador);
            assertSame(planificador, authorizationService
                    .requireGestionLogistica(planificadorAuthId, AccionAutorizada.CREAR_PRODUCTO));
        }

        @Test
        @DisplayName("Encargado de tienda no pasa y el mensaje nombra la acción")
        void encargadoTienda_noPasa() {
            autenticar(encargadoTiendaAuthId, encargadoTienda);
            AccesoDenegadoException e = assertThrows(AccesoDenegadoException.class, () -> authorizationService
                    .requireGestionLogistica(encargadoTiendaAuthId, AccionAutorizada.EDITAR_PRODUCTO));
            assertEquals("No tiene permisos para editar productos", e.getMessage());
        }

        @Test
        @DisplayName("Encargado de CD no pasa")
        void encargadoCd_noPasa() {
            autenticar(encargadoCdAuthId, encargadoCd);
            assertThrows(AccesoDenegadoException.class, () -> authorizationService
                    .requireGestionLogistica(encargadoCdAuthId, AccionAutorizada.CREAR_CENTRO_DISTRIBUCION));
        }
    }

    // ------------------------------------------------------------------
    // requirePermisoTienda
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("requirePermisoTienda")
    class RequirePermisoTienda {

        @Test
        @DisplayName("Sin identidad lanza")
        void sinIdentidad_lanza() {
            assertThrows(AccesoDenegadoException.class, () -> authorizationService.requirePermisoTienda(
                    null, tienda(ENCARGADO_TIENDA_ID), AccionAutorizada.REGISTRAR_VENTA));
        }

        @Test
        @DisplayName("ADMINISTRADOR puede operar cualquier tienda")
        void admin_pasa() {
            autenticar(adminAuthId, admin);
            assertSame(admin, authorizationService.requirePermisoTienda(
                    adminAuthId, tienda(OTRO_ENCARGADO_ID), AccionAutorizada.REGISTRAR_VENTA));
        }

        @Test
        @DisplayName("PLANIFICADOR puede operar cualquier tienda")
        void planificador_pasa() {
            autenticar(planificadorAuthId, planificador);
            assertSame(planificador, authorizationService.requirePermisoTienda(
                    planificadorAuthId, tienda(OTRO_ENCARGADO_ID), AccionAutorizada.CONSULTAR_INVENTARIO_TIENDA));
        }

        @Test
        @DisplayName("Encargado asignado a ESA tienda pasa")
        void encargadoAsignado_pasa() {
            autenticar(encargadoTiendaAuthId, encargadoTienda);
            assertSame(encargadoTienda, authorizationService.requirePermisoTienda(
                    encargadoTiendaAuthId, tienda(ENCARGADO_TIENDA_ID), AccionAutorizada.CONSULTAR_INVENTARIO_TIENDA));
        }

        @Test
        @DisplayName("Encargado de tienda NO asignado a esa tienda no pasa")
        void encargadoNoAsignado_noPasa() {
            autenticar(encargadoTiendaAuthId, encargadoTienda);
            assertThrows(AccesoDenegadoException.class, () -> authorizationService.requirePermisoTienda(
                    encargadoTiendaAuthId, tienda(OTRO_ENCARGADO_ID), AccionAutorizada.CONSULTAR_INVENTARIO_TIENDA));
        }

        @Test
        @DisplayName("Encargado de CD no puede operar una tienda")
        void encargadoCd_noPasa() {
            autenticar(encargadoCdAuthId, encargadoCd);
            assertThrows(AccesoDenegadoException.class, () -> authorizationService.requirePermisoTienda(
                    encargadoCdAuthId, tienda(ENCARGADO_CD_ID), AccionAutorizada.CONSULTAR_INVENTARIO_TIENDA));
        }

        @Test
        @DisplayName("Encargado de tienda no asignado a ninguna (encargadoId null) no pasa")
        void encargadoSinAsignacion_noPasa() {
            autenticar(encargadoTiendaAuthId, encargadoTienda);
            assertThrows(AccesoDenegadoException.class, () -> authorizationService.requirePermisoTienda(
                    encargadoTiendaAuthId, tienda(null), AccionAutorizada.CONSULTAR_INVENTARIO_TIENDA));
        }
    }

    // ------------------------------------------------------------------
    // requirePermisoCentroDistribucion
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("requirePermisoCentroDistribucion")
    class RequirePermisoCentroDistribucion {

        @Test
        @DisplayName("Sin identidad lanza")
        void sinIdentidad_lanza() {
            assertThrows(AccesoDenegadoException.class, () -> authorizationService.requirePermisoCentroDistribucion(
                    null, centro(ENCARGADO_CD_ID), AccionAutorizada.REGISTRAR_INGRESO));
        }

        @Test
        @DisplayName("ADMINISTRADOR puede operar cualquier CD")
        void admin_pasa() {
            autenticar(adminAuthId, admin);
            assertSame(admin, authorizationService.requirePermisoCentroDistribucion(
                    adminAuthId, centro(OTRO_ENCARGADO_ID), AccionAutorizada.REGISTRAR_INGRESO));
        }

        @Test
        @DisplayName("PLANIFICADOR puede operar cualquier CD")
        void planificador_pasa() {
            autenticar(planificadorAuthId, planificador);
            assertSame(planificador, authorizationService.requirePermisoCentroDistribucion(
                    planificadorAuthId, centro(OTRO_ENCARGADO_ID), AccionAutorizada.CONSULTAR_INVENTARIO_CENTRO_DISTRIBUCION));
        }

        @Test
        @DisplayName("Encargado de CD asignado a ESE centro pasa")
        void encargadoAsignado_pasa() {
            autenticar(encargadoCdAuthId, encargadoCd);
            assertSame(encargadoCd, authorizationService.requirePermisoCentroDistribucion(
                    encargadoCdAuthId, centro(ENCARGADO_CD_ID), AccionAutorizada.REGISTRAR_INGRESO));
        }

        @Test
        @DisplayName("Encargado de CD NO asignado a ese centro no pasa")
        void encargadoNoAsignado_noPasa() {
            autenticar(encargadoCdAuthId, encargadoCd);
            assertThrows(AccesoDenegadoException.class, () -> authorizationService.requirePermisoCentroDistribucion(
                    encargadoCdAuthId, centro(OTRO_ENCARGADO_ID), AccionAutorizada.REGISTRAR_INGRESO));
        }

        @Test
        @DisplayName("Encargado de tienda no puede operar un CD")
        void encargadoTienda_noPasa() {
            autenticar(encargadoTiendaAuthId, encargadoTienda);
            assertThrows(AccesoDenegadoException.class, () -> authorizationService.requirePermisoCentroDistribucion(
                    encargadoTiendaAuthId, centro(ENCARGADO_TIENDA_ID), AccionAutorizada.REGISTRAR_INGRESO));
        }
    }
}
