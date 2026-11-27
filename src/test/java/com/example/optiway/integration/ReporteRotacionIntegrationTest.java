package com.example.optiway.integration;

import com.example.optiway.domain.model.Rol;
import com.example.optiway.infrastructure.adapter.out.persistence.InventarioJpaEntity;
import com.example.optiway.infrastructure.adapter.out.persistence.InventarioJpaRepository;
import com.example.optiway.infrastructure.adapter.out.persistence.ProductoJpaEntity;
import com.example.optiway.infrastructure.adapter.out.persistence.ProductoJpaRepository;
import com.example.optiway.infrastructure.adapter.out.persistence.TiendaJpaEntity;
import com.example.optiway.infrastructure.adapter.out.persistence.TiendaJpaRepository;
import com.example.optiway.infrastructure.adapter.out.persistence.UsuarioJpaEntity;
import com.example.optiway.infrastructure.adapter.out.persistence.UsuarioJpaRepository;
import com.example.optiway.infrastructure.adapter.out.persistence.VentaJpaEntity;
import com.example.optiway.infrastructure.adapter.out.persistence.VentaJpaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReporteRotacionIntegrationTest {

    private static final LocalDate DESDE = LocalDate.of(2026, 11, 1);
    private static final LocalDate HASTA = LocalDate.of(2026, 11, 10);

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioJpaRepository usuarios;
    @Autowired private TiendaJpaRepository tiendas;
    @Autowired private ProductoJpaRepository productos;
    @Autowired private InventarioJpaRepository inventarios;
    @Autowired private VentaJpaRepository ventas;
    @Autowired private EntityManager entityManager;

    private UUID planificadorId;
    private UUID encargadoId;
    private Long tiendaId;

    @BeforeEach
    void prepararDatos() {
        planificadorId = UUID.randomUUID();
        encargadoId = UUID.randomUUID();

        usuarios.save(new UsuarioJpaEntity(
                null, planificadorId, "Planificador HU-27",
                "plan-" + planificadorId + "@optiway.test", Rol.PLANIFICADOR, null));
        usuarios.save(new UsuarioJpaEntity(
                null, encargadoId, "Encargado HU-27",
                "enc-" + encargadoId + "@optiway.test", Rol.ENCARGADO_TIENDA, null));

        TiendaJpaEntity tienda = tiendas.save(new TiendaJpaEntity(
                null, "TND-" + planificadorId.toString().substring(0, 8),
                "Tienda Norte", "Calle 10", "Medellin", true, null));
        tiendaId = tienda.getId();

        ProductoJpaEntity arroz = guardarProducto("ARZ-" + planificadorId, "=SUM(A1)", "Granos");
        ProductoJpaEntity sal = guardarProducto("SAL-" + planificadorId, "Sal Refisal", "Granos");

        inventarios.save(new InventarioJpaEntity(null, tienda, null, arroz, 10, 2));
        inventarios.save(new InventarioJpaEntity(null, tienda, null, sal, 15, 2));

        registrarVenta(tienda, arroz, DESDE.atTime(10, 0), 20);
        registrarVenta(tienda, sal, DESDE.plusDays(2).atTime(14, 0), 5);
        // Venta fuera de rango: no debe incluirse
        registrarVenta(tienda, sal, HASTA.plusDays(1).atTime(0, 0), 50);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void consultaJsonDevuelveCalculoDeRotacionYClasificacion() throws Exception {
        mockMvc.perform(get("/api/reportes/rotacion")
                        .param("tiendaId", tiendaId.toString())
                        .param("desde", DESDE.toString())
                        .param("hasta", HASTA.toString())
                        .with(jwt().jwt(token -> token.subject(planificadorId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tiendaId").value(tiendaId))
                .andExpect(jsonPath("$.productos.length()").value(2))
                .andExpect(jsonPath("$.productos[0].unidadesVendidas").value(20))
                .andExpect(jsonPath("$.productos[0].clasificacionRotacion").value("RAPIDA"))
                .andExpect(jsonPath("$.productos[1].unidadesVendidas").value(5))
                .andExpect(jsonPath("$.productos[1].clasificacionRotacion").value("BAJA"));
    }

    @Test
    void exportarCsvDevuelveBomUtf8YSanitizaFormulas() throws Exception {
        byte[] bytes = mockMvc.perform(get("/api/reportes/rotacion/exportar")
                        .param("tiendaId", tiendaId.toString())
                        .param("desde", DESDE.toString())
                        .param("hasta", HASTA.toString())
                        .with(jwt().jwt(token -> token.subject(planificadorId.toString()))))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString(".csv")))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        String csv = new String(bytes, StandardCharsets.UTF_8);
        assertTrue(csv.startsWith("\uFEFFtiendaId,desde,hasta,"));
        assertTrue(csv.contains("\"'=SUM(A1)\""));
        assertTrue(csv.contains("\"Sal Refisal\""));
    }

    @Test
    void rechazaFechasInvertidasCon400() throws Exception {
        mockMvc.perform(get("/api/reportes/rotacion")
                        .param("tiendaId", tiendaId.toString())
                        .param("desde", HASTA.toString())
                        .param("hasta", DESDE.toString())
                        .with(jwt().jwt(token -> token.subject(planificadorId.toString()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rechazaUsuarioSinRolPlanificadorCon403() throws Exception {
        mockMvc.perform(get("/api/reportes/rotacion")
                        .param("tiendaId", tiendaId.toString())
                        .param("desde", DESDE.toString())
                        .param("hasta", HASTA.toString())
                        .with(jwt().jwt(token -> token.subject(encargadoId.toString()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void devuelve404CuandoTiendaNoExiste() throws Exception {
        mockMvc.perform(get("/api/reportes/rotacion")
                        .param("tiendaId", "999999")
                        .param("desde", DESDE.toString())
                        .param("hasta", HASTA.toString())
                        .with(jwt().jwt(token -> token.subject(planificadorId.toString()))))
                .andExpect(status().isNotFound());
    }

    private ProductoJpaEntity guardarProducto(String identificador, String nombre, String categoria) {
        ProductoJpaEntity p = new ProductoJpaEntity();
        p.setIdentificador(identificador);
        p.setNombre(nombre);
        p.setCategoria(categoria);
        p.setPrecio(new BigDecimal("2500.00"));
        p.setUnidadMedida("unidad");
        p.setActivo(true);
        return productos.save(p);
    }

    private void registrarVenta(TiendaJpaEntity tienda, ProductoJpaEntity producto, LocalDateTime fecha, int cantidad) {
        VentaJpaEntity venta = new VentaJpaEntity();
        venta.setTienda(tienda);
        venta.setProducto(producto);
        venta.setCantidad(cantidad);
        venta.setFechaHora(fecha);
        ventas.save(venta);
    }
}
