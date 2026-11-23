package com.example.optiway.application.service;

import com.example.optiway.application.exception.RecursoNoEncontradoException;
import com.example.optiway.application.port.in.ConsultarPatronesDemandaUseCase;
import com.example.optiway.application.port.in.dto.PatronesDemandaResponse;
import com.example.optiway.application.port.in.dto.PatronesDemandaResponse.DemandaTienda;
import com.example.optiway.application.port.in.dto.PatronesDemandaResponse.VentaDiaria;
import com.example.optiway.application.port.out.AuthorizationPort;
import com.example.optiway.application.port.out.ProductoRepositoryPort;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.application.port.out.VentaRepositoryPort;
import com.example.optiway.domain.model.Tienda;
import com.example.optiway.domain.model.Venta;
import com.example.optiway.infrastructure.security.AuthorizedAction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ConsultarPatronesDemandaService implements ConsultarPatronesDemandaUseCase {

    private static final int MAX_TIENDAS = 50;
    private static final long MAX_DIAS = 366;

    private final AuthorizationPort autorizacion;
    private final ProductoRepositoryPort productos;
    private final TiendaRepositoryPort tiendas;
    private final VentaRepositoryPort ventas;

    public ConsultarPatronesDemandaService(
            AuthorizationPort autorizacion,
            ProductoRepositoryPort productos,
            TiendaRepositoryPort tiendas,
            VentaRepositoryPort ventas) {
        this.autorizacion = autorizacion;
        this.productos = productos;
        this.tiendas = tiendas;
        this.ventas = ventas;
    }

    @Override
    @Transactional(readOnly = true)
    public PatronesDemandaResponse consultarPatronesDemanda(
            UUID authUserId,
            Long productoId,
            List<Long> tiendaIds,
            LocalDate desde,
            LocalDate hasta) {

        autorizacion.requirePlanificador(
                authUserId, AuthorizedAction.CONSULTAR_PATRONES_DEMANDA);

        if (productoId == null || productoId <= 0) {
            throw new IllegalArgumentException("El ID del producto debe ser positivo.");
        }
        if (tiendaIds == null || tiendaIds.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos una tienda.");
        }
        if (tiendaIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("Los ID de las tiendas deben ser positivos.");
        }

        List<Long> tiendasSeleccionadas = tiendaIds.stream().distinct().toList();
        if (tiendasSeleccionadas.size() > MAX_TIENDAS) {
            throw new IllegalArgumentException(
                    "No se pueden consultar más de " + MAX_TIENDAS + " tiendas a la vez.");
        }

        if (desde == null || hasta == null || hasta.isBefore(desde)) {
            throw new IllegalArgumentException("El rango de fechas no es válido.");
        }
        if (hasta.equals(LocalDate.MAX)) {
            throw new IllegalArgumentException("La fecha final está fuera del rango admitido.");
        }
        if (ChronoUnit.DAYS.between(desde, hasta) + 1 > MAX_DIAS) {
            throw new IllegalArgumentException(
                    "El rango no puede superar " + MAX_DIAS + " días.");
        }

        productos.findById(productoId).orElseThrow(() ->
                new RecursoNoEncontradoException("No se encontró el producto " + productoId));

        Set<Long> idsEncontrados = tiendas.obtenerPorIds(tiendasSeleccionadas).stream()
                .map(Tienda::getId)
                .collect(Collectors.toSet());
        for (Long tiendaId : tiendasSeleccionadas) {
            if (!idsEncontrados.contains(tiendaId)) {
                throw new RecursoNoEncontradoException("No se encontró la tienda " + tiendaId);
            }
        }

        LocalDate finExclusivo = hasta.plusDays(1);
        List<Venta> ventasDelPeriodo = ventas.buscarVentasPorProductoYTiendasEnPeriodo(
                productoId, tiendasSeleccionadas, desde.atStartOfDay(), finExclusivo.atStartOfDay());

        Map<Long, Map<LocalDate, Long>> ventasPorTiendaYDia = new HashMap<>();
        for (Venta venta : ventasDelPeriodo) {
            Map<LocalDate, Long> ventasPorDia = ventasPorTiendaYDia.computeIfAbsent(
                    venta.getTiendaId(), id -> new HashMap<>());
            ventasPorDia.merge(
                    venta.getFechaHora().toLocalDate(),
                    venta.getCantidad().longValue(),
                    Long::sum);
        }

        List<DemandaTienda> resumen = new ArrayList<>();
        for (Long tiendaId : tiendasSeleccionadas) {
            Map<LocalDate, Long> ventasPorDia =
                    ventasPorTiendaYDia.getOrDefault(tiendaId, Map.of());

            List<VentaDiaria> evolucion = desde.datesUntil(finExclusivo)
                    .map(dia -> new VentaDiaria(dia, ventasPorDia.getOrDefault(dia, 0L)))
                    .toList();

            long total = evolucion.stream().mapToLong(VentaDiaria::unidadesVendidas).sum();
            resumen.add(new DemandaTienda(tiendaId, total, evolucion));
        }

        LongSummaryStatistics totales = resumen.stream()
                .mapToLong(DemandaTienda::unidadesVendidas)
                .summaryStatistics();

        return new PatronesDemandaResponse(
                productoId, desde, hasta, resumen,
                tiendasConTotal(resumen, totales.getMax()),
                tiendasConTotal(resumen, totales.getMin()));
    }

    private List<Long> tiendasConTotal(List<DemandaTienda> resumen, long total) {
        return resumen.stream()
                .filter(tienda -> tienda.unidadesVendidas() == total)
                .map(DemandaTienda::tiendaId)
                .toList();
    }
}