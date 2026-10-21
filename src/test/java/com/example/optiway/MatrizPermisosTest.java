package com.example.optiway;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.optiway.application.port.in.CrearCentroDistribucionCommand;
import com.example.optiway.application.port.in.CrearProductoCommand;
import com.example.optiway.application.port.in.CrearTiendaCommand;
import com.example.optiway.application.port.in.ObtenerInventarioCommand;
import com.example.optiway.application.port.in.RegistrarIngresoCommand;
import com.example.optiway.application.port.in.RegistrarVentaCommand;
import com.example.optiway.application.port.out.AuthPort;
import com.example.optiway.application.port.out.CentroDistribucionRepositoryPort;
import com.example.optiway.application.port.out.IngresoMercanciaRepositoryPort;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.application.port.out.VentaRepositoryPort;
import com.example.optiway.application.exception.AccesoDenegadoException;
import com.example.optiway.application.service.AuthErrorMapper;
import com.example.optiway.application.service.CentroDistribucionService;
import com.example.optiway.application.service.IngresoMercanciaService;
import com.example.optiway.application.service.InventarioService;
import com.example.optiway.application.service.ProductoService;
import com.example.optiway.application.service.TiendaService;
import com.example.optiway.application.service.UsuarioService;
import com.example.optiway.application.service.VentaService;
import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.domain.model.Inventario;
import com.example.optiway.domain.model.Producto;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.Tienda;
import com.example.optiway.domain.model.Usuario;
import com.example.optiway.infrastructure.security.AuthorizationService;

/**
 * Matriz de permisos RBAC (rol x operación).
 *
 * <p>Este archivo es la fuente de verdad ejecutable de la tabla de permisos del
 * sistema. Cada caso declara explícitamente si un rol DEBE poder o NO poder ejecutar
 * una operación, y se ejecuta contra los servicios reales. Si un cambio de código
 * altera la matriz, un caso falla aquí.
 *
 * <p>Reglas de negocio:
 * <ul>
 *   <li>ADMINISTRADOR: gestiona todo, incluidos usuarios y roles.</li>
 *   <li>PLANIFICADOR: gestiona todo el dominio logístico salvo usuarios y roles.</li>
 *   <li>ENCARGADO_TIENDA: sólo opera la tienda que tiene asignada.</li>
 *   <li>ENCARGADO_CD: sólo opera el centro de distribución que tiene asignado.</li>
 * </ul>
 *
 * <p>La identidad es obligatoria en todas las operaciones: sin identidad hay rechazo.
 */
@ExtendWith(MockitoExtension.class)
class MatrizPermisosTest {

    private static final Long TIENDA_ID = 10L;
    private static final Long CD_ID = 20L;
    private static final Long PRODUCTO_ID = 30L;
    private static final Long ENCARGADO_TIENDA_ID = 100L;
    private static final Long ENCARGADO_CD_ID = 200L;

    @Mock private UsuarioRepositoryPort usuarioRepositoryPort;
    @Mock private TiendaRepositoryPort tiendaRepositoryPort;
    @Mock private CentroDistribucionRepositoryPort centroDistribucionRepositoryPort;
    @Mock private ProductoRepositoryPort productoRepositoryPort;
    @Mock private InventarioRepositoryPort inventarioRepositoryPort;
    @Mock private VentaRepositoryPort ventaRepositoryPort;
    @Mock private IngresoMercanciaRepositoryPort ingresoMercanciaRepositoryPort;
    @Mock private AuthPort authPort;

    private AuthorizationService authorizationService;
    private UsuarioService usuarioService;
    private TiendaService tiendaService;
    private CentroDistribucionService centroDistribucionService;
    private ProductoService productoService;
    private VentaService ventaService;
    private IngresoMercanciaService ingresoMercanciaService;
    private InventarioService inventarioService;

    private UUID adminAuthId;
    private UUID planificadorAuthId;
    private UUID encargadoTiendaAuthId;
    private UUID encargadoCdAuthId;

    @BeforeEach
    void setUp() {
        authorizationService = new AuthorizationService(usuarioRepositoryPort);
        usuarioService = new UsuarioService(usuarioRepositoryPort, authPort, authorizationService,
                new AuthErrorMapper());
        tiendaService = new TiendaService(tiendaRepositoryPort, usuarioRepositoryPort, authorizationService);
        centroDistribucionService = new CentroDistribucionService(
                centroDistribucionRepositoryPort, usuarioRepositoryPort, authorizationService);
        productoService = new ProductoService(
                productoRepositoryPort, usuarioRepositoryPort, inventarioRepositoryPort, authorizationService);
        ventaService = new VentaService(
                ventaRepositoryPort, inventarioRepositoryPort, tiendaRepositoryPort,
                productoRepositoryPort, authorizationService);
        ingresoMercanciaService = new IngresoMercanciaService(
                ingresoMercanciaRepositoryPort, inventarioRepositoryPort,
                centroDistribucionRepositoryPort, productoRepositoryPort, authorizationService);
        inventarioService = new InventarioService(
                inventarioRepositoryPort, productoRepositoryPort, tiendaRepositoryPort,
                centroDistribucionRepositoryPort, authorizationService);

        adminAuthId = UUID.randomUUID();
        planificadorAuthId = UUID.randomUUID();
        encargadoTiendaAuthId = UUID.randomUUID();
        encargadoCdAuthId = UUID.randomUUID();

        autenticar(adminAuthId, usuario(1L, adminAuthId, Rol.ADMINISTRADOR));
        autenticar(planificadorAuthId, usuario(2L, planificadorAuthId, Rol.PLANIFICADOR));
        autenticar(encargadoTiendaAuthId,
                usuario(ENCARGADO_TIENDA_ID, encargadoTiendaAuthId, Rol.ENCARGADO_TIENDA));
        autenticar(encargadoCdAuthId,
                usuario(ENCARGADO_CD_ID, encargadoCdAuthId, Rol.ENCARGADO_CD));

        // Recursos base: la tienda asignada a ENCARGADO_TIENDA, el CD asignado a ENCARGADO_CD.
        lenient().when(tiendaRepositoryPort.obtenerPorId(TIENDA_ID)).thenReturn(Optional.of(
                new Tienda(TIENDA_ID, "TND-001", "Tienda", "Dir", "Ciudad", true, ENCARGADO_TIENDA_ID)));
        lenient().when(centroDistribucionRepositoryPort.obtenerPorId(CD_ID)).thenReturn(Optional.of(
                new CentroDistribucion(CD_ID, "CD-001", "CD", "Dir", 5000, ENCARGADO_CD_ID, null)));
        lenient().when(productoRepositoryPort.findById(PRODUCTO_ID)).thenReturn(Optional.of(
                new Producto(PRODUCTO_ID, "PRD-001", "Producto", "Cat", BigDecimal.TEN, "Unidad", true)));
        lenient().when(inventarioRepositoryPort.findByTiendaIdAndProductoId(TIENDA_ID, PRODUCTO_ID))
                .thenReturn(Optional.of(new Inventario(1L, TIENDA_ID, null, PRODUCTO_ID, 100, 5)));
        lenient().when(inventarioRepositoryPort.findByCentroDistribucionIdAndProductoId(CD_ID, PRODUCTO_ID))
                .thenReturn(Optional.of(new Inventario(2L, null, CD_ID, PRODUCTO_ID, 100, 5)));
        lenient().when(inventarioRepositoryPort.findByTiendaId(TIENDA_ID)).thenReturn(List.of());
        lenient().when(inventarioRepositoryPort.findByCentroDistribucionId(CD_ID)).thenReturn(List.of());
    }

    private void autenticar(UUID authId, Usuario usuario) {
        lenient().when(usuarioRepositoryPort.obtenerPorAuthUserId(authId)).thenReturn(Optional.of(usuario));
    }

    private Usuario usuario(Long id, UUID authId, Rol rol) {
        return new Usuario(id, authId, rol.name(), rol.name().toLowerCase() + "@optiway.com", rol, null);
    }

    // ------------------------------------------------------------------
    // Gestión de usuarios: sólo ADMINISTRADOR
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Matriz: gestión de usuarios (sólo ADMINISTRADOR)")
    class GestionUsuarios {

        @Test
        @DisplayName("ADMIN puede listar usuarios")
        void admin_puedeListarUsuarios() {
            lenient().when(usuarioRepositoryPort.obtenerTodos()).thenReturn(List.of());
            assertDoesNotThrow(() -> usuarioService.obtenerUsuarios(adminAuthId));
        }

        @Test
        @DisplayName("PLANIFICADOR no puede listar usuarios")
        void planificador_noPuedeListarUsuarios() {
            assertThrows(AccesoDenegadoException.class,
                    () -> usuarioService.obtenerUsuarios(planificadorAuthId));
        }

        @Test
        @DisplayName("ENCARGADO_TIENDA no puede listar usuarios")
        void encargadoTienda_noPuedeListarUsuarios() {
            assertThrows(AccesoDenegadoException.class,
                    () -> usuarioService.obtenerUsuarios(encargadoTiendaAuthId));
        }

        @Test
        @DisplayName("ENCARGADO_CD no puede listar usuarios")
        void encargadoCd_noPuedeListarUsuarios() {
            assertThrows(AccesoDenegadoException.class,
                    () -> usuarioService.obtenerUsuarios(encargadoCdAuthId));
        }

        @Test
        @DisplayName("Sin identidad no se puede listar usuarios")
        void sinIdentidad_noPuedeListarUsuarios() {
            assertThrows(AccesoDenegadoException.class, () -> usuarioService.obtenerUsuarios(null));
        }
    }

    @Nested
    @DisplayName("Matriz: ver detalle de usuario (propio o ADMIN)")
    class VerDetalleUsuario {

        @Test
        @DisplayName("Un usuario autenticado puede ver su propio detalle")
        void encargadoTienda_puedeVerSuPropioDetalle() {
            lenient().when(usuarioRepositoryPort.obtenerPorId(ENCARGADO_TIENDA_ID))
                    .thenReturn(Optional.of(usuario(ENCARGADO_TIENDA_ID, encargadoTiendaAuthId, Rol.ENCARGADO_TIENDA)));
            assertDoesNotThrow(() -> usuarioService.obtenerUsuarioPorId(encargadoTiendaAuthId, ENCARGADO_TIENDA_ID));
        }

        @Test
        @DisplayName("ADMIN puede ver el detalle de otro usuario")
        void admin_puedeVerDetalleDeOtro() {
            lenient().when(usuarioRepositoryPort.obtenerPorId(ENCARGADO_TIENDA_ID))
                    .thenReturn(Optional.of(usuario(ENCARGADO_TIENDA_ID, encargadoTiendaAuthId, Rol.ENCARGADO_TIENDA)));
            assertDoesNotThrow(() -> usuarioService.obtenerUsuarioPorId(adminAuthId, ENCARGADO_TIENDA_ID));
        }

        @Test
        @DisplayName("Un no-ADMIN NO puede ver el detalle de otro usuario (IDOR cerrado)")
        void encargadoTienda_noPuedeVerDetalleDeOtro() {
            lenient().when(usuarioRepositoryPort.obtenerPorId(ENCARGADO_CD_ID))
                    .thenReturn(Optional.of(usuario(ENCARGADO_CD_ID, encargadoCdAuthId, Rol.ENCARGADO_CD)));
            assertThrows(AccesoDenegadoException.class,
                    () -> usuarioService.obtenerUsuarioPorId(encargadoTiendaAuthId, ENCARGADO_CD_ID));
        }

        @Test
        @DisplayName("Sin identidad no se puede ver el detalle de nadie")
        void sinIdentidad_noPuedeVerDetalle() {
            assertThrows(AccesoDenegadoException.class,
                    () -> usuarioService.obtenerUsuarioPorId(null, ENCARGADO_TIENDA_ID));
        }

        @Test
        @DisplayName("El perfil propio (/me) exige identidad")
        void perfilPropio_exigeIdentidad() {
            assertThrows(AccesoDenegadoException.class, () -> usuarioService.obtenerUsuarioPorAuthId(null));
        }
    }

    // ------------------------------------------------------------------
    // Gestión del dominio logístico: ADMINISTRADOR y PLANIFICADOR
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Matriz: crear tienda (ADMIN y PLANIFICADOR)")
    class CrearTienda {

        private CrearTiendaCommand command() {
            return new CrearTiendaCommand("Nueva", "Dir", "Ciudad", null);
        }

        @Test
        @DisplayName("ADMIN puede crear tienda")
        void admin_puedeCrearTienda() {
            lenient().when(tiendaRepositoryPort.save(any(Tienda.class))).thenAnswer(i -> i.getArgument(0));
            assertDoesNotThrow(() -> tiendaService.crearTienda(adminAuthId, command()));
        }

        @Test
        @DisplayName("PLANIFICADOR puede crear tienda")
        void planificador_puedeCrearTienda() {
            lenient().when(tiendaRepositoryPort.save(any(Tienda.class))).thenAnswer(i -> i.getArgument(0));
            assertDoesNotThrow(() -> tiendaService.crearTienda(planificadorAuthId, command()));
        }

        @Test
        @DisplayName("ENCARGADO_TIENDA no puede crear tienda")
        void encargadoTienda_noPuedeCrearTienda() {
            assertThrows(AccesoDenegadoException.class,
                    () -> tiendaService.crearTienda(encargadoTiendaAuthId, command()));
        }

        @Test
        @DisplayName("ENCARGADO_CD no puede crear tienda")
        void encargadoCd_noPuedeCrearTienda() {
            assertThrows(AccesoDenegadoException.class,
                    () -> tiendaService.crearTienda(encargadoCdAuthId, command()));
        }
    }

    @Nested
    @DisplayName("Matriz: crear centro de distribución (ADMIN y PLANIFICADOR)")
    class CrearCentroDistribucion {

        private CrearCentroDistribucionCommand command() {
            return new CrearCentroDistribucionCommand("CD", "Dir", 1000, null, null);
        }

        @Test
        @DisplayName("ADMIN puede crear CD")
        void admin_puedeCrearCd() {
            lenient().when(centroDistribucionRepositoryPort.guardar(any(CentroDistribucion.class)))
                    .thenAnswer(i -> i.getArgument(0));
            assertDoesNotThrow(() -> centroDistribucionService.crearCentroDistribucion(adminAuthId, command()));
        }

        @Test
        @DisplayName("PLANIFICADOR puede crear CD")
        void planificador_puedeCrearCd() {
            lenient().when(centroDistribucionRepositoryPort.guardar(any(CentroDistribucion.class)))
                    .thenAnswer(i -> i.getArgument(0));
            assertDoesNotThrow(() -> centroDistribucionService.crearCentroDistribucion(planificadorAuthId, command()));
        }

        @Test
        @DisplayName("ENCARGADO_CD no puede crear CD")
        void encargadoCd_noPuedeCrearCd() {
            assertThrows(AccesoDenegadoException.class,
                    () -> centroDistribucionService.crearCentroDistribucion(encargadoCdAuthId, command()));
        }
    }

    @Nested
    @DisplayName("Matriz: crear producto (ADMIN y PLANIFICADOR)")
    class CrearProducto {

        private CrearProductoCommand command() {
            return new CrearProductoCommand("PRD-X", "Producto", "Cat", BigDecimal.TEN, "Unidad");
        }

        @Test
        @DisplayName("ADMIN puede crear producto")
        void admin_puedeCrearProducto() {
            lenient().when(productoRepositoryPort.existsByIdentificador(any())).thenReturn(false);
            lenient().when(productoRepositoryPort.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));
            assertDoesNotThrow(() -> productoService.crearProducto(adminAuthId, command()));
        }

        @Test
        @DisplayName("PLANIFICADOR puede crear producto")
        void planificador_puedeCrearProducto() {
            lenient().when(productoRepositoryPort.existsByIdentificador(any())).thenReturn(false);
            lenient().when(productoRepositoryPort.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));
            assertDoesNotThrow(() -> productoService.crearProducto(planificadorAuthId, command()));
        }

        @Test
        @DisplayName("ENCARGADO_TIENDA no puede crear producto")
        void encargadoTienda_noPuedeCrearProducto() {
            assertThrows(AccesoDenegadoException.class,
                    () -> productoService.crearProducto(encargadoTiendaAuthId, command()));
        }
    }

    // ------------------------------------------------------------------
    // Operación con ámbito: encargados limitados a su ubicación
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Matriz: registrar venta en tienda")
    class RegistrarVenta {

        private RegistrarVentaCommand command() {
            return new RegistrarVentaCommand(TIENDA_ID, PRODUCTO_ID, 1, null);
        }

        @Test
        @DisplayName("ADMIN puede vender en cualquier tienda")
        void admin_puedeVender() {
            lenient().when(ventaRepositoryPort.guardar(any())).thenAnswer(i -> i.getArgument(0));
            assertDoesNotThrow(() -> ventaService.registrarVenta(adminAuthId, command()));
        }

        @Test
        @DisplayName("PLANIFICADOR puede vender en cualquier tienda")
        void planificador_puedeVender() {
            lenient().when(ventaRepositoryPort.guardar(any())).thenAnswer(i -> i.getArgument(0));
            assertDoesNotThrow(() -> ventaService.registrarVenta(planificadorAuthId, command()));
        }

        @Test
        @DisplayName("ENCARGADO_TIENDA puede vender en SU tienda asignada")
        void encargadoTiendaAsignado_puedeVender() {
            lenient().when(ventaRepositoryPort.guardar(any())).thenAnswer(i -> i.getArgument(0));
            assertDoesNotThrow(() -> ventaService.registrarVenta(encargadoTiendaAuthId, command()));
        }

        @Test
        @DisplayName("ENCARGADO_TIENDA NO puede vender en una tienda ajena")
        void encargadoTiendaAjeno_noPuedeVender() {
            when(tiendaRepositoryPort.obtenerPorId(TIENDA_ID)).thenReturn(Optional.of(
                    new Tienda(TIENDA_ID, "TND-001", "Tienda", "Dir", "Ciudad", true, 999L)));
            assertThrows(AccesoDenegadoException.class,
                    () -> ventaService.registrarVenta(encargadoTiendaAuthId, command()));
        }

        @Test
        @DisplayName("ENCARGADO_CD NO puede vender en ninguna tienda")
        void encargadoCd_noPuedeVender() {
            assertThrows(AccesoDenegadoException.class,
                    () -> ventaService.registrarVenta(encargadoCdAuthId, command()));
        }

        @Test
        @DisplayName("Sin identidad NO se puede vender")
        void sinIdentidad_noPuedeVender() {
            assertThrows(AccesoDenegadoException.class, () -> ventaService.registrarVenta(null, command()));
        }
    }

    @Nested
    @DisplayName("Matriz: registrar ingreso en CD")
    class RegistrarIngreso {

        private RegistrarIngresoCommand command() {
            return new RegistrarIngresoCommand(CD_ID, PRODUCTO_ID, 1, "Proveedor");
        }

        @Test
        @DisplayName("ADMIN puede ingresar en cualquier CD")
        void admin_puedeIngresar() {
            lenient().when(ingresoMercanciaRepositoryPort.guardar(any())).thenAnswer(i -> i.getArgument(0));
            assertDoesNotThrow(() -> ingresoMercanciaService.registrarIngreso(adminAuthId, command()));
        }

        @Test
        @DisplayName("PLANIFICADOR puede ingresar en cualquier CD")
        void planificador_puedeIngresar() {
            lenient().when(ingresoMercanciaRepositoryPort.guardar(any())).thenAnswer(i -> i.getArgument(0));
            assertDoesNotThrow(() -> ingresoMercanciaService.registrarIngreso(planificadorAuthId, command()));
        }

        @Test
        @DisplayName("ENCARGADO_CD puede ingresar en SU centro asignado")
        void encargadoCdAsignado_puedeIngresar() {
            lenient().when(ingresoMercanciaRepositoryPort.guardar(any())).thenAnswer(i -> i.getArgument(0));
            assertDoesNotThrow(() -> ingresoMercanciaService.registrarIngreso(encargadoCdAuthId, command()));
        }

        @Test
        @DisplayName("ENCARGADO_CD NO puede ingresar en un CD ajeno")
        void encargadoCdAjeno_noPuedeIngresar() {
            when(centroDistribucionRepositoryPort.obtenerPorId(CD_ID)).thenReturn(Optional.of(
                    new CentroDistribucion(CD_ID, "CD-001", "CD", "Dir", 5000, 999L, null)));
            assertThrows(AccesoDenegadoException.class,
                    () -> ingresoMercanciaService.registrarIngreso(encargadoCdAuthId, command()));
        }

        @Test
        @DisplayName("ENCARGADO_TIENDA NO puede ingresar en un CD")
        void encargadoTienda_noPuedeIngresar() {
            assertThrows(AccesoDenegadoException.class,
                    () -> ingresoMercanciaService.registrarIngreso(encargadoTiendaAuthId, command()));
        }
    }

    @Nested
    @DisplayName("Matriz: consultar inventario")
    class ConsultarInventario {

        @Test
        @DisplayName("ADMIN puede consultar inventario de tienda")
        void admin_puedeConsultarInventarioTienda() {
            assertDoesNotThrow(() -> inventarioService.obtenerInventario(
                    adminAuthId, new ObtenerInventarioCommand(TIENDA_ID, null, null, null, null)));
        }

        @Test
        @DisplayName("ENCARGADO_TIENDA puede consultar SU inventario")
        void encargadoTiendaAsignado_puedeConsultar() {
            assertDoesNotThrow(() -> inventarioService.obtenerInventario(
                    encargadoTiendaAuthId, new ObtenerInventarioCommand(TIENDA_ID, null, null, null, null)));
        }

        @Test
        @DisplayName("ENCARGADO_TIENDA NO puede consultar inventario de tienda ajena")
        void encargadoTiendaAjeno_noPuedeConsultar() {
            when(tiendaRepositoryPort.obtenerPorId(TIENDA_ID)).thenReturn(Optional.of(
                    new Tienda(TIENDA_ID, "TND-001", "Tienda", "Dir", "Ciudad", true, 999L)));
            assertThrows(AccesoDenegadoException.class, () -> inventarioService.obtenerInventario(
                    encargadoTiendaAuthId, new ObtenerInventarioCommand(TIENDA_ID, null, null, null, null)));
        }

        @Test
        @DisplayName("ENCARGADO_CD puede consultar SU inventario de CD")
        void encargadoCdAsignado_puedeConsultar() {
            assertDoesNotThrow(() -> inventarioService.obtenerInventario(
                    encargadoCdAuthId, new ObtenerInventarioCommand(null, CD_ID, null, null, null)));
        }

        @Test
        @DisplayName("ENCARGADO_CD NO puede consultar inventario de tienda")
        void encargadoCd_noPuedeConsultarTienda() {
            assertThrows(AccesoDenegadoException.class, () -> inventarioService.obtenerInventario(
                    encargadoCdAuthId, new ObtenerInventarioCommand(TIENDA_ID, null, null, null, null)));
        }

        @Test
        @DisplayName("Sin identidad NO se puede consultar inventario de tienda")
        void sinIdentidad_noPuedeConsultar() {
            assertThrows(AccesoDenegadoException.class, () -> inventarioService.obtenerInventario(
                    null, new ObtenerInventarioCommand(TIENDA_ID, null, null, null, null)));
        }
    }

    // ------------------------------------------------------------------
    // Listados: alcance por rol
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Matriz: listado de tiendas (alcance por rol)")
    class ListarTiendas {

        @Test
        @DisplayName("ADMIN ve todas las tiendas")
        void admin_veTodas() {
            when(tiendaRepositoryPort.findAll()).thenReturn(List.of(
                    new Tienda(TIENDA_ID, "TND-001", "T", "D", "C", true, ENCARGADO_TIENDA_ID),
                    new Tienda(11L, "TND-002", "T2", "D", "C", true, null)));
            assertEquals(2, tiendaService.obtenerTiendas(adminAuthId).size());
        }

        @Test
        @DisplayName("ENCARGADO_TIENDA ve sólo las suyas")
        void encargadoTienda_veSoloLasSuyas() {
            when(tiendaRepositoryPort.obtenerPorEncargadoId(ENCARGADO_TIENDA_ID)).thenReturn(List.of(
                    new Tienda(TIENDA_ID, "TND-001", "T", "D", "C", true, ENCARGADO_TIENDA_ID)));
            assertEquals(1, tiendaService.obtenerTiendas(encargadoTiendaAuthId).size());
        }

        @Test
        @DisplayName("ENCARGADO_CD no ve ninguna tienda")
        void encargadoCd_noVeTiendas() {
            assertEquals(0, tiendaService.obtenerTiendas(encargadoCdAuthId).size());
        }

        @Test
        @DisplayName("Sin identidad no se puede listar tiendas")
        void sinIdentidad_noPuedeListar() {
            assertThrows(AccesoDenegadoException.class, () -> tiendaService.obtenerTiendas(null));
        }
    }
}
