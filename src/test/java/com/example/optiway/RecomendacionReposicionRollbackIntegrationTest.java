package com.example.optiway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.example.optiway.application.port.in.AprobarRecomendacionCommand;
import com.example.optiway.application.port.in.AprobarRecomendacionUseCase;
import com.example.optiway.application.port.out.OrdenReposicionRepositoryPort;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.infrastructure.adapter.out.persistence.CentroDistribucionJpaEntity;
import com.example.optiway.infrastructure.adapter.out.persistence.CentroDistribucionJpaRepository;
import com.example.optiway.infrastructure.adapter.out.persistence.InventarioJpaEntity;
import com.example.optiway.infrastructure.adapter.out.persistence.InventarioJpaRepository;
import com.example.optiway.infrastructure.adapter.out.persistence.OrdenReposicionJpaRepository;
import com.example.optiway.infrastructure.adapter.out.persistence.ProductoJpaEntity;
import com.example.optiway.infrastructure.adapter.out.persistence.ProductoJpaRepository;
import com.example.optiway.infrastructure.adapter.out.persistence.RecomendacionReposicionJpaEntity;
import com.example.optiway.infrastructure.adapter.out.persistence.RecomendacionReposicionJpaRepository;
import com.example.optiway.infrastructure.adapter.out.persistence.TiendaJpaEntity;
import com.example.optiway.infrastructure.adapter.out.persistence.TiendaJpaRepository;
import com.example.optiway.infrastructure.adapter.out.persistence.UsuarioJpaEntity;
import com.example.optiway.infrastructure.adapter.out.persistence.UsuarioJpaRepository;

@SpringBootTest
class RecomendacionReposicionRollbackIntegrationTest {

    @Autowired private AprobarRecomendacionUseCase aprobarUseCase;
    @Autowired private UsuarioJpaRepository usuarioRepository;
    @Autowired private ProductoJpaRepository productoRepository;
    @Autowired private TiendaJpaRepository tiendaRepository;
    @Autowired private CentroDistribucionJpaRepository centroRepository;
    @Autowired private InventarioJpaRepository inventarioRepository;
    @Autowired private RecomendacionReposicionJpaRepository recomendacionRepository;
    @Autowired private OrdenReposicionJpaRepository ordenRepository;

    @MockitoSpyBean
    private OrdenReposicionRepositoryPort ordenRepositoryPort;

    private Long createdRecomendacionId;
    private Long createdInventarioId;
    private Long createdCentroId;
    private Long createdTiendaId;
    private Long createdProductoId;
    private Long createdUsuarioId;

    @AfterEach
    void limpiar() {
        if (createdRecomendacionId != null) {
            ordenRepository.deleteByRecomendacion_Id(createdRecomendacionId);
            recomendacionRepository.deleteById(createdRecomendacionId);
        }
        if (createdInventarioId != null) {
            inventarioRepository.deleteById(createdInventarioId);
        }
        if (createdCentroId != null) {
            centroRepository.deleteById(createdCentroId);
        }
        if (createdTiendaId != null) {
            tiendaRepository.deleteById(createdTiendaId);
        }
        if (createdProductoId != null) {
            productoRepository.deleteById(createdProductoId);
        }
        if (createdUsuarioId != null) {
            usuarioRepository.deleteById(createdUsuarioId);
        }
    }

    @Test
    void aprobarRecomendacion_CuandoFallaPersistenciaDeOrden_DebeHacerRollbackDeRecomendacion() {
        UUID planificadorAuthId = UUID.randomUUID();
        UsuarioJpaEntity planificador = new UsuarioJpaEntity();
        planificador.setAuthUserId(planificadorAuthId);
        planificador.setNombre("Planificador Rollback");
        planificador.setEmail("rollback-" + planificadorAuthId + "@optiway.com");
        planificador.setRol(Rol.PLANIFICADOR);
        planificador = usuarioRepository.save(planificador);
        createdUsuarioId = planificador.getId();

        ProductoJpaEntity producto = new ProductoJpaEntity();
        producto.setIdentificador("PRD-RB-" + UUID.randomUUID().toString().substring(0, 6));
        producto.setNombre("Producto Rollback");
        producto.setCategoria("General");
        producto.setPrecio(new BigDecimal("15000"));
        producto.setUnidadMedida("Unidad");
        producto.setActivo(true);
        producto = productoRepository.save(producto);
        createdProductoId = producto.getId();

        TiendaJpaEntity tienda = new TiendaJpaEntity();
        tienda.setCodigo("TND-RB-" + UUID.randomUUID().toString().substring(0, 6));
        tienda.setNombre("Tienda Rollback");
        tienda.setDireccion("Calle 1");
        tienda.setCiudad("Medellín");
        tienda.setActivo(true);
        tienda = tiendaRepository.save(tienda);
        createdTiendaId = tienda.getId();

        CentroDistribucionJpaEntity centro = new CentroDistribucionJpaEntity();
        centro.setCodigo("CD-RB-" + UUID.randomUUID().toString().substring(0, 6));
        centro.setNombre("CD Rollback");
        centro.setDireccion("Autopista 1");
        centro.setCapacidad(1000);
        centro.setEstado("Activo");
        centro.setTiendasAbastecidas(List.of(tienda));
        centro = centroRepository.save(centro);
        createdCentroId = centro.getId();

        InventarioJpaEntity inventario = new InventarioJpaEntity();
        inventario.setCentroDistribucion(centro);
        inventario.setProducto(producto);
        inventario.setCantidadActual(100);
        inventario.setStockMinimo(10);
        inventario = inventarioRepository.save(inventario);
        createdInventarioId = inventario.getId();

        RecomendacionReposicionJpaEntity propuesta = new RecomendacionReposicionJpaEntity();
        propuesta.setProducto(producto);
        propuesta.setCantidad(20);
        propuesta.setCentroDistribucionOrigen(centro);
        propuesta.setTiendaDestino(tienda);
        propuesta.setEstado("PROPUESTA");
        propuesta.setFueraDeCobertura(false);
        propuesta.setCreador(planificador);
        propuesta.setFechaCreacion(LocalDateTime.now());
        propuesta = recomendacionRepository.save(propuesta);
        createdRecomendacionId = propuesta.getId();

        Long recomendacionId = propuesta.getId();

        RuntimeException falloSimulado = new RuntimeException("Fallo simulado al persistir orden");
        doThrow(falloSimulado)
                .when(ordenRepositoryPort).guardar(any());

        org.junit.jupiter.api.Assertions.assertSame(falloSimulado, assertThrows(RuntimeException.class, () ->
                aprobarUseCase.aprobar(
                        planificadorAuthId,
                        new AprobarRecomendacionCommand(recomendacionId, 30, null, null))));

        RecomendacionReposicionJpaEntity trasRollback =
                recomendacionRepository.findById(recomendacionId).orElseThrow();

        assertEquals("PROPUESTA", trasRollback.getEstado());
        assertEquals(20, trasRollback.getCantidad());
        assertNull(trasRollback.getCantidadPropuesta());
        assertNull(trasRollback.getAccionTomada());
        assertNull(trasRollback.getResueltoPor());
        assertNull(trasRollback.getFechaResolucion());
        assertEquals(0, ordenRepository.countByRecomendacion_Id(recomendacionId));
    }
}
