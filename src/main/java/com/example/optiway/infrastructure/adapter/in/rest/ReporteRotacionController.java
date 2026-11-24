package com.example.optiway.infrastructure.adapter.in.rest;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.optiway.application.port.in.ReporteRotacionUseCase;
import com.example.optiway.application.port.in.dto.ReporteRotacionProductoResponse;
import com.example.optiway.application.port.in.dto.ReporteRotacionResponse;
import com.example.optiway.infrastructure.adapter.in.rest.docs.ApiBadRequestResponse;
import com.example.optiway.infrastructure.adapter.in.rest.docs.ApiCommonErrorResponses;
import com.example.optiway.infrastructure.adapter.in.rest.docs.ApiResourceNotFoundResponse;
import com.example.optiway.infrastructure.security.AuthUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/reportes/rotacion")
@Tag(name = "Reportes", description = "Reportes operativos y exportación de datos")
public class ReporteRotacionController {

    private static final MediaType CSV_UTF_8 = MediaType.parseMediaType("text/csv;charset=UTF-8");
    private final ReporteRotacionUseCase reporteRotacionUseCase;

    public ReporteRotacionController(ReporteRotacionUseCase reporteRotacionUseCase) {
        this.reporteRotacionUseCase = reporteRotacionUseCase;
    }

    @GetMapping
    @ApiBadRequestResponse
    @ApiResourceNotFoundResponse
    @ApiCommonErrorResponses
    @Operation(summary = "Consultar reporte de rotación", description = "Agrupa unidades vendidas, tiempo promedio estimado en inventario y clasificación de rotación por producto. Requiere rol PLANIFICADOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reporte generado correctamente")
    })
    public ReporteRotacionResponse consultar(
            @AuthUserId UUID authUserId,
            @Parameter(description = "ID numérico de la tienda", required = true, example = "1")
            @RequestParam Long tiendaId,
            @Parameter(description = "Primer día incluido, formato ISO-8601", required = true, example = "2026-11-01")
            @RequestParam LocalDate desde,
            @Parameter(description = "Último día incluido, formato ISO-8601", required = true, example = "2026-11-30")
            @RequestParam LocalDate hasta) {
        return reporteRotacionUseCase.generarReporte(authUserId, tiendaId, desde, hasta);
    }

    @GetMapping(value = "/exportar", produces = "text/csv")
    @ApiBadRequestResponse
    @ApiResourceNotFoundResponse
    @ApiCommonErrorResponses
    @Operation(summary = "Exportar reporte de rotación a CSV", description = "Descarga el mismo reporte de rotación en CSV UTF-8 compatible con hojas de cálculo. Requiere rol PLANIFICADOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Archivo CSV generado correctamente")
    })
    public ResponseEntity<byte[]> exportar(
            @AuthUserId UUID authUserId,
            @RequestParam Long tiendaId,
            @RequestParam LocalDate desde,
            @RequestParam LocalDate hasta) {
        ReporteRotacionResponse reporte = reporteRotacionUseCase.generarReporte(authUserId, tiendaId, desde, hasta);
        byte[] contenido = convertirCsv(reporte).getBytes(StandardCharsets.UTF_8);
        String nombreArchivo = "reporte-rotacion-tienda-" + tiendaId + "-" + desde + "-" + hasta + ".csv";
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(CSV_UTF_8)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nombreArchivo, StandardCharsets.UTF_8).build().toString())
                .body(contenido);
    }

    private String convertirCsv(ReporteRotacionResponse reporte) {
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("tiendaId,desde,hasta,productoId,nombreProducto,categoriaProducto,unidadesVendidas,stockActual,tiempoPromedioEstimadoEnInventarioDias,clasificacionRotacion\r\n");
        for (ReporteRotacionProductoResponse producto : reporte.productos()) {
            csv.append(reporte.tiendaId()).append(',')
                    .append(reporte.desde()).append(',')
                    .append(reporte.hasta()).append(',')
                    .append(producto.productoId()).append(',')
                    .append(campoTexto(producto.nombreProducto())).append(',')
                    .append(campoTexto(producto.categoriaProducto())).append(',')
                    .append(producto.unidadesVendidas()).append(',')
                    .append(producto.stockActual()).append(',')
                    .append(producto.tiempoPromedioEstimadoEnInventarioDias() == null ? "" : producto.tiempoPromedioEstimadoEnInventarioDias().toPlainString()).append(',')
                    .append(producto.clasificacionRotacion())
                    .append("\r\n");
        }
        return csv.toString();
    }

    private String campoTexto(String valor) {
        if (valor == null) {
            return "\"\"";
        }
        String seguro = valor.matches("^[\\s]*[=+@\\-].*") ? "'" + valor : valor;
        return "\"" + seguro.replace("\"", "\"\"") + "\"";
    }
}
