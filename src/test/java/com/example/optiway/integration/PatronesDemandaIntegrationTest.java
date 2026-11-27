package com.example.optiway.integration;

import com.example.optiway.domain.model.Rol;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.LongStream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PatronesDemandaIntegrationTest {

    private static final LocalDate DESDE = LocalDate.of(2026, 11, 1);
    private static final LocalDate HASTA = LocalDate.of(2026, 11, 3);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioJpaRepository usuarios;

    @Autowired
    private TiendaJpaRepository tiendas;

    @Autowired
    private ProductoJpaRepository productos;

    @Autowired
    private VentaJpaRepository ventas;

    @Autowired
    private EntityManager entityManager;

    private UUID planificadorId;
    private Long productoId;
    private Long tienda1Id;
    private Long tienda2Id;

    @BeforeEach
    void prepararDatos() {
        planificadorId = UUID.randomUUID();
        usuarios.save(new UsuarioJpaEntity(
                null, planificadorId, "Planificador HU-19",
                "planificador-" + planificadorId + "@optiway.test", Rol.PLANIFICADOR, null));

        TiendaJpaEntity tienda1 = tiendas.save(nuevaTienda("TND-1-" + planificadorId));
        TiendaJpaEntity tienda2 = tiendas.save(nuevaTienda("TND-2-" + planificadorId));
        TiendaJpaEntity tienda3 = tiendas.save(nuevaTienda("TND-3-" + planificadorId));

        ProductoJpaEntity producto = productos.save(nuevoProducto("PROD-1-" + planificadorId));
        ProductoJpaEntity otroProducto = productos.save(nuevoProducto("PROD-2-" + planificadorId));

        // Fuera del rango: no debe contarse.
        venta(tienda1, producto, DESDE.minusDays(1).atTime(12, 0), 100);
        venta(tienda1, producto, HASTA.plusDays(1).atTime(0, 0), 100);

        // Tienda 1: 2 + 3 el primer día y 4 el último = 9.
        venta(tienda1, producto, DESDE.atTime(10, 0), 2);
        venta(tienda1, producto, DESDE.atTime(23, 59), 3);
        venta(tienda1, producto, HASTA.atTime(12, 0), 4);

        // Tienda 2: 5 unidades el primer día.
        venta(tienda2, producto, DESDE.atTime(12, 0), 5);

        // Ruido: otro producto y una tienda no seleccionada.
        venta(tienda1, otroProducto, DESDE.atTime(12, 0), 50);
        venta(tienda3, producto, DESDE.atTime(12, 0), 50);

        tienda1Id = tienda1.getId();
        tienda2Id = tienda2.getId();
        productoId = producto.getId();

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void consultaConTiendasRepetidasDevuelveSerieAgrupadaYExcluyeLimitesDelRango() throws Exception {
        mockMvc.perform(request(productoId, List.of(tienda1Id, tienda2Id, tienda1Id))
                        .with(jwt().jwt(token -> token.subject(planificadorId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productoId").value(productoId))
                .andExpect(jsonPath("$.desde").value(DESDE.toString()))
                .andExpect(jsonPath("$.hasta").value(HASTA.toString()))
                .andExpect(jsonPath("$.tiendas.length()").value(2))
                .andExpect(jsonPath("$.tiendas[0].tiendaId").value(tienda1Id))
                .andExpect(jsonPath("$.tiendas[0].unidadesVendidas").value(9))
                .andExpect(jsonPath("$.tiendas[0].evolucion.length()").value(3))
                .andExpect(jsonPath("$.tiendas[0].evolucion[1].unidadesVendidas").value(0))
                .andExpect(jsonPath("$.tiendas[1].tiendaId").value(tienda2Id))
                .andExpect(jsonPath("$.tiendas[1].unidadesVendidas").value(5))
                .andExpect(jsonPath("$.tiendasMayorRotacion[0]").value(tienda1Id))
                .andExpect(jsonPath("$.tiendasMenorRotacion[0]").value(tienda2Id));
    }

    @Test
    void aceptaTiendaIdsSeparadosPorComa() throws Exception {
        UriComponentsBuilder uri = uriBase(productoId)
                .queryParam("tiendaIds", tienda1Id + "," + tienda2Id);

        mockMvc.perform(get(uri.toUriString())
                        .with(jwt().jwt(token -> token.subject(planificadorId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tiendas.length()").value(2))
                .andExpect(jsonPath("$.tiendas[0].unidadesVendidas").value(9))
                .andExpect(jsonPath("$.tiendas[1].unidadesVendidas").value(5));
    }

    @Test
    void rechazaFechaConFormatoNoIso() throws Exception {
        UriComponentsBuilder uri = UriComponentsBuilder.fromPath("/api/demanda/patrones")
                .queryParam("productoId", productoId)
                .queryParam("tiendaIds", tienda1Id)
                .queryParam("desde", "01-11-2026")
                .queryParam("hasta", HASTA.toString());

        mockMvc.perform(get(uri.toUriString())
                        .with(jwt().jwt(token -> token.subject(planificadorId.toString()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void rechazaRangoSuperiorATrescientosSesentaYSeisDias() throws Exception {
        UriComponentsBuilder uri = UriComponentsBuilder.fromPath("/api/demanda/patrones")
                .queryParam("productoId", productoId)
                .queryParam("tiendaIds", tienda1Id)
                .queryParam("desde", DESDE.minusYears(1).toString())
                .queryParam("hasta", HASTA.toString());

        mockMvc.perform(get(uri.toUriString())
                        .with(jwt().jwt(token -> token.subject(planificadorId.toString()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El rango no puede superar 366 días."));
    }

    @Test
    void rechazaMasDeCincuentaTiendas() throws Exception {
        List<Long> muchasTiendas = LongStream.rangeClosed(1, 51).boxed().toList();
        UriComponentsBuilder uri = uriBase(productoId)
                .queryParam("tiendaIds", muchasTiendas.toArray());

        mockMvc.perform(get(uri.toUriString())
                        .with(jwt().jwt(token -> token.subject(planificadorId.toString()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No se pueden consultar más de 50 tiendas a la vez."));
    }

    @Test
    void devuelve403ParaUnAdministrador() throws Exception {
        UUID administradorId = UUID.randomUUID();
        usuarios.save(new UsuarioJpaEntity(
                null, administradorId, "Admin HU-19",
                "admin-" + administradorId + "@optiway.test", Rol.ADMINISTRADOR, null));
        entityManager.flush();

        mockMvc.perform(request(productoId, List.of(tienda1Id))
                        .with(jwt().jwt(token -> token.subject(administradorId.toString()))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void devuelve404CuandoNoExisteElProductoOAlgunaTienda() throws Exception {
        mockMvc.perform(request(999_999L, List.of(tienda1Id))
                        .with(jwt().jwt(token -> token.subject(planificadorId.toString()))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No se encontró el producto 999999"));

        mockMvc.perform(request(productoId, List.of(999_999L))
                        .with(jwt().jwt(token -> token.subject(planificadorId.toString()))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No se encontró la tienda 999999"));
    }

    private MockHttpServletRequestBuilder request(Long productoId, List<Long> tiendaIds) {
        return get(uriBase(productoId)
                .queryParam("tiendaIds", tiendaIds.toArray())
                .toUriString());
    }

    private UriComponentsBuilder uriBase(Long productoId) {
        return UriComponentsBuilder.fromPath("/api/demanda/patrones")
                .queryParam("productoId", productoId)
                .queryParam("desde", DESDE.toString())
                .queryParam("hasta", HASTA.toString());
    }

    private TiendaJpaEntity nuevaTienda(String codigo) {
        return new TiendaJpaEntity(null, codigo, "Tienda " + codigo, "Dirección", "Bogotá", true, null);
    }

    private ProductoJpaEntity nuevoProducto(String identificador) {
        ProductoJpaEntity producto = new ProductoJpaEntity();
        producto.setNombre("Producto " + identificador);
        producto.setCategoria("Pruebas");
        producto.setIdentificador(identificador);
        producto.setPrecio(new BigDecimal("1000.00"));
        producto.setUnidadMedida("unidad");
        producto.setActivo(true);
        return producto;
    }

    private void venta(TiendaJpaEntity tienda, ProductoJpaEntity producto,
                       LocalDateTime fechaHora, int cantidad) {
        ventas.save(new VentaJpaEntity(null, tienda, producto, cantidad, fechaHora));
    }
}
