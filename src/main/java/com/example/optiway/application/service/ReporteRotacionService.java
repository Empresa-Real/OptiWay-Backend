package com.example.optiway.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.optiway.application.exception.RecursoNoEncontradoException;
import com.example.optiway.application.port.in.ReporteRotacionUseCase;
import com.example.optiway.application.port.in.dto.ReporteRotacionProductoResponse;
import com.example.optiway.application.port.in.dto.ReporteRotacionResponse;
import com.example.optiway.application.port.out.AuthorizationPort;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.application.port.out.VentaRepositoryPort;
import com.example.optiway.domain.model.Producto;
import com.example.optiway.infrastructure.security.AuthorizedAction;

@Service
public class ReporteRotacionService implements ReporteRotacionUseCase {

    private static final String METODO_CALCULO = "Tiempo estimado = días del periodo × ((stockActual + unidadesVendidas / 2) / unidadesVendidas). "
            + "stockActual es la existencia consultada hoy y se usa como aproximación del stock al cierre del periodo; el stock inicial se estima como stockActual + unidadesVendidas. "
            + "No se registran entradas de mercancía en tiendas, por lo que esta métrica no reconstruye movimientos históricos. "
            + "RAPIDA/BAJA compara unidades vendidas por día con la mediana de productos de la misma tienda y periodo.";

    private final VentaRepositoryPort ventaRepository;
    private final InventarioRepositoryPort inventarioRepository;
    private final ProductoRepositoryPort productoRepository;
    private final TiendaRepositoryPort tiendaRepository;
    private final AuthorizationPort authorization;

    public ReporteRotacionService(
            VentaRepositoryPort ventaRepository,
            InventarioRepositoryPort inventarioRepository,
            ProductoRepositoryPort productoRepository,
            TiendaRepositoryPort tiendaRepository,
            AuthorizationPort authorization) {
        this.ventaRepository = ventaRepository;
        this.inventarioRepository = inventarioRepository;
        this.productoRepository = productoRepository;
        this.tiendaRepository = tiendaRepository;
        this.authorization = authorization;
    }

    @Override
    public ReporteRotacionResponse generarReporte(UUID authUserId, Long tiendaId, LocalDate desde, LocalDate hasta) {
        validarParametros(tiendaId, desde, hasta);
        authorization.requirePlanificador(authUserId, AuthorizedAction.CONSULTAR_REPORTE_ROTACION);
        var tienda = tiendaRepository.obtenerPorId(tiendaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La tienda con ID " + tiendaId + " no fue encontrada."));

        var ventas = ventaRepository.resumirUnidadesVendidasPorProducto(
                tiendaId, desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
        Map<Long, Long> ventasPorProducto = new HashMap<>();
        ventas.forEach(venta -> ventasPorProducto.put(venta.productoId(),
                venta.unidadesVendidas() == null ? 0L : venta.unidadesVendidas()));
        Map<Long, Integer> stockPorProducto = new HashMap<>();
        inventarioRepository.findByTiendaId(tiendaId).forEach(inventario ->
                stockPorProducto.merge(inventario.getProductoId(),
                        inventario.getCantidadActual() == null ? 0 : inventario.getCantidadActual(), Integer::sum));

        java.util.Set<Long> productoIds = new java.util.TreeSet<>();
        productoIds.addAll(ventasPorProducto.keySet());
        productoIds.addAll(stockPorProducto.keySet());
        Map<Long, Producto> productos = productoRepository.findAllById(List.copyOf(productoIds)).stream()
                .collect(java.util.stream.Collectors.toMap(Producto::getId, producto -> producto));

        long diasPeriodo = ChronoUnit.DAYS.between(desde, hasta) + 1;
        List<ReporteRotacionProductoResponse> filasSinClasificar = productoIds.stream()
                .map(productoId -> {
                    Producto producto = productos.get(productoId);
                    int stockActual = Math.max(0, stockPorProducto.getOrDefault(productoId, 0));
                    long unidadesVendidas = ventasPorProducto.getOrDefault(productoId, 0L);
                    BigDecimal diasPromedio = unidadesVendidas == 0 ? null
                            : BigDecimal.valueOf(diasPeriodo)
                                    .multiply(BigDecimal.valueOf(stockActual).add(BigDecimal.valueOf(unidadesVendidas)
                                            .divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP)))
                                    .divide(BigDecimal.valueOf(unidadesVendidas), 2, RoundingMode.HALF_UP);
                    return new ReporteRotacionProductoResponse(
                            productoId,
                            producto == null ? "Producto " + productoId : producto.getNombre(),
                            producto == null ? null : producto.getCategoria(),
                            unidadesVendidas,
                            stockActual,
                            diasPromedio,
                            unidadesVendidas == 0 ? "SIN_VENTAS" : null);
                })
                .toList();

        List<BigDecimal> velocidades = filasSinClasificar.stream()
                .filter(fila -> fila.unidadesVendidas() > 0)
                .map(fila -> BigDecimal.valueOf(fila.unidadesVendidas()).divide(BigDecimal.valueOf(diasPeriodo), 4, RoundingMode.HALF_UP))
                .sorted()
                .toList();
        BigDecimal mediana = calcularMediana(velocidades);
        List<ReporteRotacionProductoResponse> filas = filasSinClasificar.stream()
                .map(fila -> fila.unidadesVendidas() == 0 ? fila
                        : new ReporteRotacionProductoResponse(fila.productoId(), fila.nombreProducto(),
                                fila.categoriaProducto(), fila.unidadesVendidas(), fila.stockActual(),
                                fila.tiempoPromedioEstimadoEnInventarioDias(),
                                velocidad(fila.unidadesVendidas(), diasPeriodo).compareTo(mediana) >= 0 ? "RAPIDA" : "BAJA"))
                .sorted(Comparator.comparingLong(ReporteRotacionProductoResponse::unidadesVendidas).reversed()
                        .thenComparing(ReporteRotacionProductoResponse::nombreProducto, String.CASE_INSENSITIVE_ORDER))
                .toList();

        return new ReporteRotacionResponse(tiendaId, desde, hasta, METODO_CALCULO, filas);
    }

    private BigDecimal calcularMediana(List<BigDecimal> valores) {
        if (valores.isEmpty()) return BigDecimal.ZERO;
        int centro = valores.size() / 2;
        if (valores.size() % 2 == 1) return valores.get(centro);
        return valores.get(centro - 1).add(valores.get(centro)).divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal velocidad(long unidadesVendidas, long diasPeriodo) {
        return BigDecimal.valueOf(unidadesVendidas).divide(BigDecimal.valueOf(diasPeriodo), 4, RoundingMode.HALF_UP);
    }

    private void validarParametros(Long tiendaId, LocalDate desde, LocalDate hasta) {
        if (tiendaId == null || tiendaId <= 0) {
            throw new IllegalArgumentException("tiendaId debe ser un entero positivo.");
        }
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Las fechas desde y hasta son obligatorias.");
        }
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a hasta.");
        }
    }
}
