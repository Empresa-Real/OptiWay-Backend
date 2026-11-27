# Fix 48 — Consulta de órdenes por Centro de Distribución y estandarización de unidades

**Fecha:** 24 de noviembre de 2026  
**Área:** Dominio, Aplicación, Controladores REST, OpenAPI y Pruebas  
**Referencia:** Commits `10189e3` y `c69154f` en `main`

---

## 1. Contexto y Problema

1. **Órdenes entrantes en CD:** al aprobar recomendaciones (HU-21) se persistían órdenes en `OrdenReposicionJpaEntity`, pero el frontend de `ENCARGADO_CD` no disponía de ningún endpoint para listar las órdenes cuyo origen fuera su centro de distribución.
2. **Unidades de medida inconsistentes:** `ProductoService` aceptaba `unidadMedida` como texto libre (`"kg"`, `"KG"`, `"kilogramos"`, `"Litro"`, `"und"`, etc.), generando inconsistencias en base de datos y sin validación de valores permitidos.

---

## 2. Solución Implementada

- **Consulta de órdenes de CD:**
  - Agregada la acción `LISTAR_ORDENES_CENTRO_DISTRIBUCION` en `AuthorizedAction` y el puerto de entrada `ObtenerOrdenesCDUseCase`.
  - Implementado `listarPorCentroDistribucionOrigen` en `OrdenReposicionRepositoryPort` / `OrdenReposicionJpaRepository` (`findByCentroDistribucionOrigen_IdOrderByFechaCreacionDescIdDesc`) y `OrdenReposicionRepositoryAdapter`.
  - Expuesto `GET /api/centros-distribucion/{id}/ordenes` (y alias `GET /api/cd/{id}/ordenes`) en `CentroDistribucionController`, validando permisos con `requirePermisoCentroDistribucion` (`ENCARGADO_CD` asignado, `ADMINISTRADOR` o `PLANIFICADOR`).
- **Estandarización de unidades (`UnidadMedida`):**
  - Creado el enum `UnidadMedida` con catálogo estándar (`UNIDAD`, `KG`, `G`, `LITRO`, `ML`, `CAJA`, `PAQUETE`, `DOCENA`), mapa de alias en minúsculas y método `normalizar(String)` que rechaza valores fuera del catálogo con `IllegalArgumentException` (`400`).
  - Integrado en `ProductoService.crearProducto` y `editarProducto`, y documentado con `allowableValues` en `CrearProductoRequest` y `EditarProductoRequest`.

---

## 3. Archivos Principales

| Archivo | Cambio |
|---|---|
| `application/port/in/ObtenerOrdenesCDUseCase.java` | Nuevo puerto de entrada para consultar órdenes por CD |
| `application/service/CentroDistribucionService.java` | Implementa `obtenerOrdenesPorCentro` con validación de ID, `404` y permisos |
| `infrastructure/adapter/in/rest/CentroDistribucionController.java` | Expone `GET /{id}/ordenes` con anotaciones `@Api*Response` |
| `infrastructure/adapter/out/persistence/OrdenReposicionJpaRepository.java` | Consulta ordenada por `fechaCreacion DESC, id DESC` |
| `domain/model/UnidadMedida.java` | Catálogo estándar de unidades y normalizador de alias |
| `application/service/ProductoService.java` | Aplica `UnidadMedida.normalizar` al crear y editar productos |
| `src/test/java/.../CentroDistribucionOrdenesIntegrationTest.java` | Pruebas `MockMvc` (`200`, `403`, `404`) |
| `src/test/java/.../ProductoServiceTest.java` | Pruebas de alias y rechazo de unidades inválidas |

---

## 4. Verificación

- `CentroDistribucionOrdenesIntegrationTest` (3/3) + `CentroDistribucionServiceTest` (10/10): **13/13** en verde (`BUILD SUCCESS`).
- `ProductoServiceTest`: **15/15** en verde (`BUILD SUCCESS`).
