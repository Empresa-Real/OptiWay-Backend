package com.example.optiway.integration;

import com.example.optiway.domain.model.Rol;
import com.example.optiway.infrastructure.adapter.out.persistence.CentroDistribucionJpaEntity;
import com.example.optiway.infrastructure.adapter.out.persistence.CentroDistribucionJpaRepository;
import com.example.optiway.infrastructure.adapter.out.persistence.OrdenReposicionJpaEntity;
import com.example.optiway.infrastructure.adapter.out.persistence.OrdenReposicionJpaRepository;
import com.example.optiway.infrastructure.adapter.out.persistence.ProductoJpaEntity;
import com.example.optiway.infrastructure.adapter.out.persistence.ProductoJpaRepository;
import com.example.optiway.infrastructure.adapter.out.persistence.RecomendacionReposicionJpaEntity;
import com.example.optiway.infrastructure.adapter.out.persistence.RecomendacionReposicionJpaRepository;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CentroDistribucionOrdenesIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioJpaRepository usuarioRepository;
    @Autowired private ProductoJpaRepository productoRepository;
    @Autowired private TiendaJpaRepository tiendaRepository;
    @Autowired private CentroDistribucionJpaRepository centroRepository;
    @Autowired private RecomendacionReposicionJpaRepository recomendacionRepository;
    @Autowired private OrdenReposicionJpaRepository ordenRepository;
    @Autowired private EntityManager entityManager;

    private UUID encargadoAsignadoAuthId;
    private UUID otroEncargadoAuthId;
    private Long centroId;
    private Long tiendaId;
    private Long productoId;
    private Long recomendacionId;
    private Long ordenId;

    @BeforeEach
    void prepararDatos() {
        encargadoAsignadoAuthId = UUID.randomUUID();
        otroEncargadoAuthId = UUID.randomUUID();

        UsuarioJpaEntity encargado = usuarioRepository.save(new UsuarioJpaEntity(
                null, encargadoAsignadoAuthId, "Encargado CD",
                "enc-cd-" + encargadoAsignadoAuthId + "@optiway.test", Rol.ENCARGADO_CD, null));
        usuarioRepository.save(new UsuarioJpaEntity(
                null, otroEncargadoAuthId, "Otro Encargado CD",
                "otro-cd-" + otroEncargadoAuthId + "@optiway.test", Rol.ENCARGADO_CD, null));

        ProductoJpaEntity producto = new ProductoJpaEntity();
        producto.setIdentificador("PRD-ORD-" + UUID.randomUUID().toString().substring(0, 6));
        producto.setNombre("Arroz Diana");
        producto.setCategoria("Granos");
        producto.setPrecio(new BigDecimal("5000"));
        producto.setUnidadMedida("Unidad");
        producto.setActivo(true);
        producto = productoRepository.save(producto);
        productoId = producto.getId();

        TiendaJpaEntity tienda = new TiendaJpaEntity();
        tienda.setCodigo("TND-ORD-" + UUID.randomUUID().toString().substring(0, 6));
        tienda.setNombre("Tienda Destino");
        tienda.setDireccion("Calle 1");
        tienda.setCiudad("Medellín");
        tienda.setActivo(true);
        tienda = tiendaRepository.save(tienda);
        tiendaId = tienda.getId();

        CentroDistribucionJpaEntity centro = new CentroDistribucionJpaEntity();
        centro.setCodigo("CD-ORD-" + UUID.randomUUID().toString().substring(0, 6));
        centro.setNombre("CD Principal");
        centro.setDireccion("Autopista 1");
        centro.setCapacidad(1000);
        centro.setEstado("Activo");
        centro.setEncargado(encargado);
        centro.setTiendasAbastecidas(List.of(tienda));
        centro = centroRepository.save(centro);
        centroId = centro.getId();

        RecomendacionReposicionJpaEntity rec = new RecomendacionReposicionJpaEntity();
        rec.setProducto(producto);
        rec.setCantidad(25);
        rec.setCentroDistribucionOrigen(centro);
        rec.setTiendaDestino(tienda);
        rec.setEstado("APROBADA");
        rec.setFueraDeCobertura(false);
        rec.setCreador(encargado);
        rec.setFechaCreacion(LocalDateTime.now());
        rec = recomendacionRepository.save(rec);
        recomendacionId = rec.getId();

        OrdenReposicionJpaEntity orden = new OrdenReposicionJpaEntity();
        orden.setRecomendacion(rec);
        orden.setProducto(producto);
        orden.setCantidad(25);
        orden.setCentroDistribucionOrigen(centro);
        orden.setTiendaDestino(tienda);
        orden.setEstado("APROBADA");
        orden.setCreador(encargado);
        orden.setFechaCreacion(LocalDateTime.now());
        orden = ordenRepository.save(orden);
        ordenId = orden.getId();

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void encargadoAsignadoPuedeConsultarOrdenesDeSuCentroYAliasCd() throws Exception {
        mockMvc.perform(get("/api/centros-distribucion/{id}/ordenes", centroId)
                        .with(jwt().jwt(token -> token.subject(encargadoAsignadoAuthId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(ordenId))
                .andExpect(jsonPath("$[0].recomendacionId").value(recomendacionId))
                .andExpect(jsonPath("$[0].productoId").value(productoId))
                .andExpect(jsonPath("$[0].nombreProducto").value("Arroz Diana"))
                .andExpect(jsonPath("$[0].cantidad").value(25))
                .andExpect(jsonPath("$[0].centroDistribucionOrigenId").value(centroId))
                .andExpect(jsonPath("$[0].tiendaDestinoId").value(tiendaId))
                .andExpect(jsonPath("$[0].estado").value("APROBADA"));

        mockMvc.perform(get("/api/cd/{id}/ordenes", centroId)
                        .with(jwt().jwt(token -> token.subject(encargadoAsignadoAuthId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void otroEncargadoCdRecibe403() throws Exception {
        mockMvc.perform(get("/api/centros-distribucion/{id}/ordenes", centroId)
                        .with(jwt().jwt(token -> token.subject(otroEncargadoAuthId.toString()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void centroInexistenteRecibe404() throws Exception {
        mockMvc.perform(get("/api/centros-distribucion/{id}/ordenes", 999999L)
                        .with(jwt().jwt(token -> token.subject(encargadoAsignadoAuthId.toString()))))
                .andExpect(status().isNotFound());
    }
}
