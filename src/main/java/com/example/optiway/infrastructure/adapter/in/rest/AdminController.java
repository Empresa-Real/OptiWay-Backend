package com.example.optiway.infrastructure.adapter.in.rest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.example.optiway.application.port.out.CentroDistribucionRepositoryPort;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.domain.model.Inventario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Panel de observabilidad minimo: pagina /admin, ultimas lineas de log, metricas basicas de JVM
 * y estadisticas para el login. Todo publico, sin token ni cookie.
 */
@RestController
@Tag(name = "Admin", description = "Panel de observabilidad (publico, sin autenticacion)")
public class AdminController {

    private static final long INICIO = System.currentTimeMillis();

    private static final String HTML = """
            <!doctype html>
            <html lang="es">
            <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>OptiWay - Panel de administracion</title>
            <link rel="stylesheet" href="/admin.css">
            </head>
            <body>
            <main>
            <h1>OptiWay - Panel de administracion</h1>
            <p class="nota">Panel publico de observabilidad: no pide token ni inicio de sesion.</p>
            <p><button id="actualizar">Actualizar</button> <span id="estado"></span></p>
            <h2>Salud</h2><pre id="health">-</pre>
            <h2>Estadísticas</h2><pre id="estadisticas">-</pre>
            <h2>Métricas</h2><pre id="metrics">-</pre>
            <h2>Últimas líneas de log</h2><pre id="logs">-</pre>
            </main>
            <script src="/admin.js"></script>
            </body>
            </html>
            """;

    private final String logPath;
    private final TiendaRepositoryPort tiendaRepositoryPort;
    private final CentroDistribucionRepositoryPort centroDistribucionRepositoryPort;
    private final InventarioRepositoryPort inventarioRepositoryPort;

    public AdminController(
            @Value("${logging.file.name:logs/optiway.log}") String logPath,
            TiendaRepositoryPort tiendaRepositoryPort,
            CentroDistribucionRepositoryPort centroDistribucionRepositoryPort,
            InventarioRepositoryPort inventarioRepositoryPort) {
        this.logPath = logPath;
        this.tiendaRepositoryPort = tiendaRepositoryPort;
        this.centroDistribucionRepositoryPort = centroDistribucionRepositoryPort;
        this.inventarioRepositoryPort = inventarioRepositoryPort;
    }

    @GetMapping(value = "/admin", produces = MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "Panel de observabilidad", description = "Pagina HTML publica, sin token.")
    public ResponseEntity<String> panel() {
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(HTML);
    }

    @GetMapping("/admin/logs")
    @Operation(summary = "Ultimas lineas de log", description = "Lee logs/optiway.log. Publico, sin token.")
    public Map<String, Object> logs(@RequestParam(defaultValue = "100") int lineas) {
        int cantidad = Math.max(1, Math.min(lineas, 500));
        Path ruta = Path.of(logPath);
        if (!Files.exists(ruta)) {
            // En Render el filesystem es efimero: puede no existir tras un deploy.
            return Map.of("archivo", logPath, "disponible", false, "lineas", List.of());
        }
        try {
            List<String> todas = Files.readAllLines(ruta, StandardCharsets.UTF_8);
            int desde = Math.max(0, todas.size() - cantidad);
            return Map.of("archivo", logPath, "disponible", true,
                    "totalLineas", todas.size(), "lineas", todas.subList(desde, todas.size()));
        } catch (IOException excepcion) {
            return Map.of("archivo", logPath, "disponible", false,
                    "error", excepcion.getMessage(), "lineas", List.of());
        }
    }

    @GetMapping("/api/estadisticas")
    @Operation(summary = "Estadisticas del sistema", description = "Tiendas, CDs y stock. Publico: se muestra en el login.")
    public Map<String, Object> estadisticas() {
        var inventarios = inventarioRepositoryPort.findAll();
        return Map.of(
                "tiendas", tiendaRepositoryPort.findAll().size(),
                "centrosDistribucion", centroDistribucionRepositoryPort.obtenerTodos().size(),
                "unidadesStock", unidades(inventarios),
                "unidadesEnTiendas", unidades(inventarios.stream()
                        .filter(inventario -> inventario.getTiendaId() != null).toList()),
                "unidadesEnCentros", unidades(inventarios.stream()
                        .filter(inventario -> inventario.getCentroDistribucionId() != null).toList()),
                "registrosInventario", inventarios.size());
    }

    private static long unidades(List<Inventario> inventarios) {
        return inventarios.stream()
                .mapToLong(inventario -> inventario.getCantidadActual() == null ? 0 : inventario.getCantidadActual())
                .sum();
    }

    @GetMapping("/admin/metrics")
    @Operation(summary = "Metricas basicas de JVM", description = "Uptime, memoria e hilos. Publico, sin token.")
    public Map<String, Object> metricas() {
        Runtime runtime = Runtime.getRuntime();
        return Map.of(
                "uptimeSegundos", (System.currentTimeMillis() - INICIO) / 1000,
                "memoriaUsadaMB", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024),
                "memoriaTotalMB", runtime.totalMemory() / (1024 * 1024),
                "hilosActivos", Thread.activeCount(),
                "timestamp", Instant.now().toString());
    }
}
