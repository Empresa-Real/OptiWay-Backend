package com.example.optiway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.example.optiway.application.exception.AccesoDenegadoException;
import com.example.optiway.application.port.out.AuthorizationPort;
import com.example.optiway.application.port.out.CentroDistribucionRepositoryPort;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.domain.model.Inventario;
import com.example.optiway.domain.model.Tienda;
import com.example.optiway.infrastructure.adapter.in.rest.AdminController;
import com.example.optiway.infrastructure.security.AuthorizedAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class AdminControllerTest {

    private static final Path LOG = Path.of("target", "admin-controller-test.log");

    private final AuthorizationPort authorization = mock(AuthorizationPort.class);
    private final TiendaRepositoryPort tiendas = mock(TiendaRepositoryPort.class);
    private final CentroDistribucionRepositoryPort centros = mock(CentroDistribucionRepositoryPort.class);
    private final InventarioRepositoryPort inventarios = mock(InventarioRepositoryPort.class);
    private final UUID administrador = UUID.randomUUID();
    private final UUID otro = UUID.randomUUID();

    private AdminController controller;

    @BeforeEach
    void preparar() throws Exception {
        controller = new AdminController(authorization, LOG.toString(), tiendas, centros, inventarios);
        Files.writeString(LOG, "linea 1\nlinea 2\nlinea 3\n", StandardCharsets.UTF_8);
        when(tiendas.findAll()).thenReturn(List.of(mock(Tienda.class), mock(Tienda.class)));
        when(centros.obtenerTodos()).thenReturn(List.of(mock(CentroDistribucion.class)));
        when(inventarios.findAll()).thenReturn(List.of(
                new Inventario(1L, 10L, null, 100L, 20, 5),
                new Inventario(2L, null, 20L, 100L, 30, 5)));
    }

    @Test
    void panelSeSirveParaAdministrador() {
        ResponseEntity<String> respuesta = controller.panel(administrador);

        assertEquals(200, respuesta.getStatusCode().value());
        assertTrue(respuesta.getBody().contains("Panel de administracion"));
    }

    @Test
    void logsDevuelveSoloLasUltimasLineas() {
        @SuppressWarnings("unchecked")
        Map<String, Object> respuesta = (Map<String, Object>) controller.logs(administrador, 2);

        assertEquals(List.of("linea 2", "linea 3"), respuesta.get("lineas"));
        assertEquals(true, respuesta.get("disponible"));
    }

    @Test
    void metricasIncluyenUptimeYMemoria() {
        Map<String, Object> respuesta = controller.metricas(administrador);

        assertTrue(respuesta.containsKey("uptimeSegundos"));
        assertTrue(respuesta.containsKey("memoriaUsadaMB"));
    }

    @Test
    void estadisticasPublicasDevuelveConteosYStock() {
        Map<String, Object> respuesta = controller.estadisticas();

        assertEquals(2, respuesta.get("tiendas"));
        assertEquals(1, respuesta.get("centrosDistribucion"));
        assertEquals(50L, respuesta.get("unidadesStock"));
        assertEquals(20L, respuesta.get("unidadesEnTiendas"));
        assertEquals(30L, respuesta.get("unidadesEnCentros"));
    }

    @Test
    void estadisticasNoExigeAutorizacion() {
        controller.estadisticas();

        verify(authorization, never()).requireAdministrador(any(), any(AuthorizedAction.class));
    }

    @Test
    void sinRolAdministradorSeDeniega() {
        doThrow(new AccesoDenegadoException("sin permiso"))
                .when(authorization).requireAdministrador(eq(otro), any(AuthorizedAction.class));

        assertThrows(AccesoDenegadoException.class, () -> controller.logs(otro, 10));
        assertThrows(AccesoDenegadoException.class, () -> controller.metricas(otro));
        assertThrows(AccesoDenegadoException.class, () -> controller.panel(otro));
    }

    @Test
    void sinArchivoDeLogRespondeSinLineas() {
        AdminController sinArchivo = new AdminController(
                authorization, "target/no-existe.log", tiendas, centros, inventarios);

        @SuppressWarnings("unchecked")
        Map<String, Object> respuesta = (Map<String, Object>) sinArchivo.logs(administrador, 10);

        assertFalse((boolean) respuesta.get("disponible"));
        assertEquals(List.of(), respuesta.get("lineas"));
    }
}
