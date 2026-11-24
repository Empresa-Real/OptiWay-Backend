package com.example.optiway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.optiway.infrastructure.adapter.out.persistence.CentroDistribucionJpaRepository;
import com.example.optiway.infrastructure.adapter.out.persistence.IngresoMercanciaJpaRepository;
import com.example.optiway.infrastructure.adapter.out.persistence.InventarioJpaRepository;
import com.example.optiway.infrastructure.adapter.out.persistence.TiendaJpaRepository;
import com.example.optiway.infrastructure.adapter.out.persistence.VentaJpaRepository;

/**
 * Las consultas derivadas de Spring Data se construyen en la primera ejecución,
 * no al arrancar el contexto. Sin este test, una consulta con un atributo
 * inexistente (p. ej. `findByEncargadoId` cuando la entidad tiene `encargado`)
 * compila, arranca y explota en producción con 500.
 */
@SpringBootTest
@DisplayName("Consultas derivadas de los repositorios JPA")
class RepositoriosConsultasDerivadasTest {

    @Autowired
    private TiendaJpaRepository tiendas;

    @Autowired
    private CentroDistribucionJpaRepository centros;

    @Autowired
    private InventarioJpaRepository inventarios;

    @Autowired
    private IngresoMercanciaJpaRepository ingresos;

    @Autowired
    private VentaJpaRepository ventas;

    @Test
    @DisplayName("las consultas por relación se construyen y ejecutan")
    void consultasPorRelacionSeEjecutan() {
        long inexistente = 999_999L;

        assertEquals(0, tiendas.findByEncargado_Id(inexistente).size());
        assertEquals(0, centros.findByEncargado_Id(inexistente).size());

        assertEquals(0, inventarios.findByTienda_Id(inexistente).size());
        assertEquals(0, inventarios.findByCentroDistribucion_Id(inexistente).size());
        assertEquals(0, inventarios.findByTienda_IdAndProducto_Id(inexistente, inexistente).stream().count());
        assertEquals(0, inventarios.findByCentroDistribucion_IdAndProducto_Id(inexistente, inexistente).stream().count());
        assertFalse(inventarios.existsByProducto_IdAndCantidadActualGreaterThan(inexistente, 0));

        assertEquals(0, ingresos.findByCentroDistribucion_Id(inexistente).size());
        assertEquals(0, ventas.findByTienda_Id(inexistente).size());
    }
}
