package com.example.optiway.infrastructure.adapter.in.rest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.example.optiway.application.port.out.AuthorizationPort;
import com.example.optiway.application.port.out.CentroDistribucionRepositoryPort;
import com.example.optiway.application.port.out.InventarioRepositoryPort;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.domain.model.Inventario;
import com.example.optiway.infrastructure.security.AuthUserId;
import com.example.optiway.infrastructure.security.AuthorizedAction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Panel de observabilidad minimo: pagina /admin, ultimas lineas de log y metricas basicas de JVM.
 * Las tres operaciones exigen rol ADMINISTRADOR.
 */
@RestController
@Tag(name = "Admin", description = "Panel de observabilidad (solo ADMINISTRADOR)")
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
            <p class="nota">Solo ADMINISTRADOR. Pegá tu token JWT para consultar logs y métricas.</p>
            <p><label>Token JWT <input id="token" type="password" placeholder="Bearer ..." size="60"></label></p>
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

    private final AuthorizationPort authorizationService;
    private final String logPath;
    private final TiendaRepositoryPort tiendaRepositoryPort;
    private final CentroDistribucionRepositoryPort centroDistribucionRepositoryPort;
    private final InventarioRepositoryPort inventarioRepositoryPort;

    public AdminController(
            AuthorizationPort authorizationService,
            @Value("${logging.file.name:logs/optiway.log}") String logPath,
            TiendaRepositoryPort tiendaRepositoryPort,
            CentroDistribucionRepositoryPort centroDistribucionRepositoryPort,
            InventarioRepositoryPort inventarioRepositoryPort) {
        this.authorizationService = authorizationService;
        this.logPath = logPath;
        this.tiendaRepositoryPort = tiendaRepositoryPort;
        this.centroDistribucionRepositoryPort = centroDistribucionRepositoryPort;
        this.inventarioRepositoryPort = inventarioRepositoryPort;
    }

    @GetMapping(value = "/admin", produces = MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "Panel de observabilidad", description = "Pagina HTML con health, metricas y logs. Requiere rol ADMINISTRADOR.")
    public ResponseEntity<String> panel(@AuthUserId UUID authUserId) {
        authorizationService.requireAdministrador(authUserId, AuthorizedAction.VER_LOGS);
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(HTML);
    }

    @GetMapping("/admin/logs")
    @Operation(summary = "Ultimas lineas de log", description = "Lee el archivo logs/optiway.log. Requiere rol ADMINISTRADOR.")
    public Map<String, Object> logs(
            @AuthUserId UUID authUserId,
            @RequestParam(defaultValue = "100") int lineas) {
        authorizationService.requireAdministrador(authUserId, AuthorizedAction.VER_LOGS);

        int cantidad = Math.max(1, Math.min(lineas, 500));
        Path ruta = Path.of(logPath);
        if (!Files.exists(ruta)) {
            // En Render el filesystem es efímero: puede no existir tras un deploy.
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
    @Operation(summary = "Estadisticas del sistema", description = "Numero de tiendas, centros de distribucion y unidades de stock. Publico: se muestra en el login, antes de autenticarse.")
    public Map<String, Object> estadisticas() {
        // Publico a proposito: el frontend lo consulta en el login, sin token ni cookie.
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
    @Operation(summary = "Metricas basicas de JVM", description = "Uptime, memoria e hilos. Requiere rol ADMINISTRADOR.")
    public Map<String, Object> metricas(@AuthUserId UUID authUserId) {
        authorizationService.requireAdministrador(authUserId, AuthorizedAction.VER_LOGS);

        Runtime runtime = Runtime.getRuntime();
        return Map.of(
                "uptimeSegundos", (System.currentTimeMillis() - INICIO) / 1000,
                "memoriaUsadaMB", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024),
                "memoriaTotalMB", runtime.totalMemory() / (1024 * 1024),
                "hilosActivos", Thread.activeCount(),
                "timestamp", Instant.now().toString());
    }
}
