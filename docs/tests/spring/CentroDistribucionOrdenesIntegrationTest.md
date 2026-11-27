# `CentroDistribucionOrdenesIntegrationTest`

- **Ubicación:** `src/test/java/com/example/optiway/integration/CentroDistribucionOrdenesIntegrationTest.java`
- **Endpoints:** `GET /api/centros-distribucion/{id}/ordenes` y `GET /api/cd/{id}/ordenes`
- **Tecnología:** `@SpringBootTest`, `@AutoConfigureMockMvc`, `@Transactional`, H2 en memoria y `jwt()` (`spring-security-test`).

## Casos de prueba (3)

1. **`encargadoAsignadoPuedeConsultarOrdenesDeSuCentroYAliasCd`**
   - Autentica al `ENCARGADO_CD` asignado al centro y consulta ambas rutas (`/api/centros-distribucion/{id}/ordenes` y `/api/cd/{id}/ordenes`).
   - Verifica `200 OK` y todos los campos de `OrdenReposicionResponse` (`id`, `recomendacionId`, `productoId`, `nombreProducto`, `cantidad`, `centroDistribucionOrigenId`, `tiendaDestinoId`, `estado`).
2. **`otroEncargadoCdRecibe403`**
   - Autentica a un `ENCARGADO_CD` distinto al asignado y verifica `403 Forbidden`.
3. **`centroInexistenteRecibe404`**
   - Consulta el ID `999999` y verifica `404 Not Found`.

## Ejecución
```powershell
./mvnw.cmd test "-Dtest=CentroDistribucionOrdenesIntegrationTest"
```
