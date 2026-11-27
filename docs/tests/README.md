# Documentación de Pruebas — OptiWay Backend

Índice de las pruebas de **integración Spring (`@SpringBootTest` / `MockMvc`)** y las suites **HTTP / E2E en Python (`tests/api/`)**.

> Las pruebas unitarias (`src/test/java/com/example/optiway/unit/`, 227 casos) se mantienen organizadas por capa (`application`, `domain`, `infrastructure`) sin documentación individual por su volumen.

---

## 1. Pruebas de Integración Spring (`src/test/java/com/example/optiway/integration/` — 21 casos)

| Suite | Archivo de documentación | Casos |
|---|---|---:|
| `AjustarInventarioIntegrationTest` | [AjustarInventarioIntegrationTest.md](spring/AjustarInventarioIntegrationTest.md) | 3 |
| `CentroDistribucionOrdenesIntegrationTest` | [CentroDistribucionOrdenesIntegrationTest.md](spring/CentroDistribucionOrdenesIntegrationTest.md) | 3 |
| `PatronesDemandaIntegrationTest` | [PatronesDemandaIntegrationTest.md](spring/PatronesDemandaIntegrationTest.md) | 7 |
| `ReporteRotacionIntegrationTest` | [ReporteRotacionIntegrationTest.md](spring/ReporteRotacionIntegrationTest.md) | 5 |
| `RecomendacionReposicionRollbackIntegrationTest` | [RecomendacionReposicionRollbackIntegrationTest.md](spring/RecomendacionReposicionRollbackIntegrationTest.md) | 1 |
| `RepositoriosConsultasDerivadasTest` | [RepositoriosConsultasDerivadasTest.md](spring/RepositoriosConsultasDerivadasTest.md) | 1 |
| `OptiwayApplicationTests` | [OptiwayApplicationTests.md](spring/OptiwayApplicationTests.md) | 1 |

Ejecución en Maven:
```powershell
./mvnw.cmd test "-Dtest=com.example.optiway.integration.*"
```

---

## 2. Scripts de Pruebas HTTP / E2E en Python (`tests/api/`)

| Script | Archivo de documentación | Propósito |
|---|---|---|
| `tests/api/test_suite.py` | [test_suite.md](python/test_suite.md) | Suite integral de contrato HTTP y flujos CRUD (Sprint 1–3) |
| `tests/api/endpoints.py` | [endpoints.md](python/endpoints.md) | Recorrido de endpoints por rol |
| `tests/api/endpoints_errores.py` | [endpoints_errores.md](python/endpoints_errores.md) | Verificación de códigos `400`, `401` y `404` |
| `tests/api/endpoints_permisos.py` | [endpoints_permisos.md](python/endpoints_permisos.md) | Matriz de autorización `403` vs `200`/`201` por rol |
| `tests/api/endpoints_supabase.py` | [endpoints_supabase.md](python/endpoints_supabase.md) | Pruebas de autenticación e invitación contra Supabase Auth |
