# `ReporteRotacionIntegrationTest`

- **Ubicación:** `src/test/java/com/example/optiway/integration/ReporteRotacionIntegrationTest.java`
- **Endpoints:** `GET /api/reportes/rotacion` y `GET /api/reportes/rotacion/exportar` (HU-27)
- **Tecnología:** `@SpringBootTest`, `@AutoConfigureMockMvc`, `@Transactional`, H2 en memoria y `jwt()` (`spring-security-test`).

## Casos de prueba (5)

1. **`consultaJsonDevuelveCalculoDeRotacionYClasificacion`**: Consulta JSON para `[desde, hasta]`, excluye ventas fuera de rango y valida unidades vendidas y clasificación (`RAPIDA` / `BAJA`).
2. **`exportarCsvDevuelveBomUtf8YSanitizaFormulas`**: Descarga CSV desde `/exportar`, verifica `Content-Disposition: attachment` (`.csv`), prefijo BOM UTF-8 (`\uFEFF`) y sanitización contra *CSV injection* (`=SUM(A1)` → `'=SUM(A1)`).
3. **`rechazaFechasInvertidasCon400`**: Envía `desde > hasta` y verifica `400 Bad Request`.
4. **`rechazaUsuarioSinRolPlanificadorCon403`**: Un `ENCARGADO_TIENDA` recibe `403 Forbidden`.
5. **`devuelve404CuandoTiendaNoExiste`**: Consulta con `tiendaId=999999` y verifica `404 Not Found`.

## Ejecución
```powershell
./mvnw.cmd test "-Dtest=ReporteRotacionIntegrationTest"
```
