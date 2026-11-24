package com.example.optiway;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.optiway.application.exception.AccesoDenegadoException;
import com.example.optiway.application.port.in.dto.SolicitudReposicionResponse;
import com.example.optiway.application.port.out.AuthorizationPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.SolicitudReposicionRepositoryPort;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.application.service.ReposicionService;
import com.example.optiway.domain.model.Producto;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.SolicitudReposicion;
import com.example.optiway.domain.model.Tienda;
import com.example.optiway.domain.model.Usuario;
import com.example.optiway.infrastructure.adapter.in.rest.dto.CrearSolicitudReposicionRequest;
import com.example.optiway.infrastructure.security.AuthorizedAction;

@ExtendWith(MockitoExtension.class)
class ReposicionServiceTest {

    @Mock
    private SolicitudReposicionRepositoryPort solicitudRepositoryPort;

    @Mock
    private TiendaRepositoryPort tiendaRepositoryPort;

    @Mock
    private ProductoRepositoryPort productoRepositoryPort;

    @Mock
    private AuthorizationPort authorizationService;

    @InjectMocks
    private ReposicionService reposicionService;

    private UUID authUserId;
    private Tienda tienda;
    private Producto producto;
    private Usuario encargado;
    private Usuario planificador;

    @BeforeEach
    void setUp() {
        authUserId = UUID.randomUUID();

        tienda = new Tienda();
        tienda.setId(1L);
        tienda.setNombre("Tienda Norte");
        tienda.setActivo(true);

        producto = new Producto();
        producto.setId(10L);
        producto.setNombre("Café Especial 500g");

        encargado = new Usuario();
        encargado.setId(5L);
        encargado.setRol(Rol.ENCARGADO_TIENDA);

        planificador = new Usuario();
        planificador.setId(8L);
        planificador.setRol(Rol.PLANIFICADOR);
    }

    @Test
    void crearSolicitud_encargadoValido_guardaYRetornaPendiente() {
        CrearSolicitudReposicionRequest request = new CrearSolicitudReposicionRequest();
        request.setProductoId(10L);
        request.setCantidad(15);

        when(tiendaRepositoryPort.obtenerPorId(1L)).thenReturn(Optional.of(tienda));
        when(authorizationService.requirePermisoTienda(authUserId, tienda, AuthorizedAction.SOLICITAR_REPOSICION))
                .thenReturn(encargado);
        when(productoRepositoryPort.findById(10L)).thenReturn(Optional.of(producto));
        when(solicitudRepositoryPort.save(any(SolicitudReposicion.class))).thenAnswer(inv -> {
            SolicitudReposicion s = inv.getArgument(0);
            s.setId(100L);
            return s;
        });

        SolicitudReposicionResponse response = reposicionService.crearSolicitud(authUserId, 1L, request);

        assertEquals(100L, response.id());
        assertEquals(1L, response.tiendaId());
        assertEquals(10L, response.productoId());
        assertEquals("Café Especial 500g", response.nombreProducto());
        assertEquals(15, response.cantidad());
        assertEquals("PENDIENTE", response.estado());
        assertEquals(5L, response.usuarioId());
        verify(solicitudRepositoryPort).save(any(SolicitudReposicion.class));
    }

    @Test
    void crearSolicitud_usuarioNoEncargado_lanzaAccesoDenegado() {
        CrearSolicitudReposicionRequest request = new CrearSolicitudReposicionRequest();
        request.setProductoId(10L);
        request.setCantidad(5);

        when(tiendaRepositoryPort.obtenerPorId(1L)).thenReturn(Optional.of(tienda));
        when(authorizationService.requirePermisoTienda(authUserId, tienda, AuthorizedAction.SOLICITAR_REPOSICION))
                .thenReturn(planificador);

        assertThrows(AccesoDenegadoException.class, () -> reposicionService.crearSolicitud(authUserId, 1L, request));
        verify(solicitudRepositoryPort, never()).save(any());
    }

    @Test
    void obtenerTodasLasSolicitudes_planificador_retornaListaDesdeBD() {
        SolicitudReposicion solicitud = new SolicitudReposicion(
                50L, 1L, 10L, 20, "PENDIENTE", 5L, LocalDateTime.now()
        );

        when(authorizationService.requireGestionLogistica(authUserId, AuthorizedAction.LISTAR_SOLICITUDES_REPOSICION))
                .thenReturn(planificador);
        when(solicitudRepositoryPort.findAll()).thenReturn(List.of(solicitud));
        when(productoRepositoryPort.findById(10L)).thenReturn(Optional.of(producto));

        List<SolicitudReposicionResponse> resultado = reposicionService.obtenerTodasLasSolicitudes(authUserId);

        assertEquals(1, resultado.size());
        assertEquals(50L, resultado.get(0).id());
        assertEquals("Café Especial 500g", resultado.get(0).nombreProducto());
    }
}