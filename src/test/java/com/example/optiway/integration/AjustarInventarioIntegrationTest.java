package com.example.optiway;

import com.example.optiway.domain.model.Rol;
import com.example.optiway.infrastructure.adapter.out.persistence.AjusteInventarioJpaRepository;
import com.example.optiway.infrastructure.adapter.out.persistence.InventarioJpaEntity;
import com.example.optiway.infrastructure.adapter.out.persistence.InventarioJpaRepository;
import com.example.optiway.infrastructure.adapter.out.persistence.ProductoJpaEntity;
import com.example.optiway.infrastructure.adapter.out.persistence.ProductoJpaRepository;
import com.example.optiway.infrastructure.adapter.out.persistence.TiendaJpaEntity;
import com.example.optiway.infrastructure.adapter.out.persistence.TiendaJpaRepository;
import com.example.optiway.infrastructure.adapter.out.persistence.UsuarioJpaEntity;
import com.example.optiway.infrastructure.adapter.out.persistence.UsuarioJpaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AjustarInventarioIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioJpaRepository usuarioRepository;

    @Autowired
    private TiendaJpaRepository tiendaRepository;

    @Autowired
    private ProductoJpaRepository productoRepository;

    @Autowired
    private InventarioJpaRepository inventarioRepository;

    @Autowired
    private AjusteInventarioJpaRepository ajusteRepository;

    @Autowired
    private EntityManager entityManager;

    private UUID authUserId;
    private Long tiendaId;
    private Long productoId;

    @BeforeEach
    void prepararDatos() {
        authUserId = UUID.randomUUID();

        usuarioRepository.save(
                new UsuarioJpaEntity(
                        null,
                        authUserId,
                        "Usuario de pruebas",
                        "prueba-" + authUserId + "@optiway.test",
                        Rol.PLANIFICADOR,
                        null
                )
        );

        TiendaJpaEntity tienda = tiendaRepository.save(
                new TiendaJpaEntity(
                        null,
                        "TEST-" + authUserId,
                        "Tienda de pruebas",
                        "Dirección de pruebas",
                        "Bogotá",
                        true,
                        null
                )
        );

        ProductoJpaEntity producto = new ProductoJpaEntity();
        producto.setNombre("Producto de pruebas");
        producto.setCategoria("Pruebas");
        producto.setIdentificador("PROD-" + authUserId);
        producto.setPrecio(new BigDecimal("1000.00"));
        producto.setUnidadMedida("unidad");
        producto.setActivo(true);
        producto = productoRepository.save(producto);

        InventarioJpaEntity inventario = new InventarioJpaEntity(
                null,
                tienda,
                null,
                producto,
                20,
                5
        );
        inventarioRepository.save(inventario);

        tiendaId = tienda.getId();
        productoId = producto.getId();

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void debeAumentarInventarioYRegistrarHistorial() throws Exception {
        mockMvc.perform(post("/api/inventario/ajustes")
                        .with(jwt().jwt(token -> token.subject(authUserId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tiendaId": %d,
                                    "productoId": %d,
                                    "nuevaCantidad": 30
                                }
                                """.formatted(tiendaId, productoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cantidadAnterior").value(20))
                .andExpect(jsonPath("$.diferencia").value(10))
                .andExpect(jsonPath("$.cantidadNueva").value(30));

        InventarioJpaEntity inventario = inventarioRepository
                .findByTienda_IdAndProducto_Id(tiendaId, productoId)
                .orElseThrow();
        assertEquals(30, inventario.getCantidadActual());

        var ajustes = ajusteRepository.findByTienda_Id(tiendaId);
        assertEquals(1, ajustes.size());
        assertEquals(20, ajustes.get(0).getCantidadAnterior());
        assertEquals(10, ajustes.get(0).getDiferencia());
        assertEquals(30, ajustes.get(0).getCantidadNueva());
    }

    @Test
    void debeDisminuirInventarioYRegistrarHistorial() throws Exception {
        mockMvc.perform(post("/api/inventario/ajustes")
                        .with(jwt().jwt(token -> token.subject(authUserId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tiendaId": %d,
                                    "productoId": %d,
                                    "nuevaCantidad": 12
                                }
                                """.formatted(tiendaId, productoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cantidadAnterior").value(20))
                .andExpect(jsonPath("$.diferencia").value(-8))
                .andExpect(jsonPath("$.cantidadNueva").value(12));

        InventarioJpaEntity inventario = inventarioRepository
                .findByTienda_IdAndProducto_Id(tiendaId, productoId)
                .orElseThrow();
        assertEquals(12, inventario.getCantidadActual());

        var ajustes = ajusteRepository.findByTienda_Id(tiendaId);
        assertEquals(1, ajustes.size());
        assertEquals(20, ajustes.get(0).getCantidadAnterior());
        assertEquals(-8, ajustes.get(0).getDiferencia());
        assertEquals(12, ajustes.get(0).getCantidadNueva());
    }

    @Test
    void noDebePermitirInventarioNegativoNiRegistrarHistorial() throws Exception {
        mockMvc.perform(post("/api/inventario/ajustes")
                        .with(jwt().jwt(token -> token.subject(authUserId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tiendaId": %d,
                                    "productoId": %d,
                                    "nuevaCantidad": -1
                                }
                                """.formatted(tiendaId, productoId)))
                .andExpect(status().isBadRequest());

        InventarioJpaEntity inventario = inventarioRepository
                .findByTienda_IdAndProducto_Id(tiendaId, productoId)
                .orElseThrow();
        assertEquals(20, inventario.getCantidadActual());

        var ajustes = ajusteRepository.findByTienda_Id(tiendaId);
        assertTrue(ajustes.isEmpty());
    }
}
