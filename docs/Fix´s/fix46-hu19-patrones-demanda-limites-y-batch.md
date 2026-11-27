# Fix 46 — Límites de consulta, carga batch y pruebas de patrones de demanda (HU-19)

**Fecha:** 23 de noviembre de 2026  
**Área:** Aplicación, Persistencia, OpenAPI y Pruebas  
**Referencia:** PR #21 (`feature/hu-19-patrones-demanda`, commits `0ebe922`, `07c7c78`, `e3c4963`)

---

## 1. Contexto y Problema

La primera versión de `ConsultarPatronesDemandaService` cubría únicamente 3 escenarios felices en pruebas unitarias y no acotaba el tamaño máximo de consultas sobre rangos históricos ni el número de tiendas por petición, exponiendo el endpoint `GET /api/demanda/patrones` a consultas excesivas en base de datos.

---

## 2. Solución Implementada

- **Límites y validaciones en `ConsultarPatronesDemandaService`:**
  - Rango máximo permitido de **366 días** y máximo **50 tiendas** por consulta.
  - Rechazo de `productoId <= 0`, `tiendaIds` nulo/vacío/con IDs inválidos, rangos invertidos y `LocalDate.MAX`.
  - Deduplicación de `tiendaIds` repetidos y consulta en lote (*batch*) sobre `VentaJpaRepository.buscarVentasPorProductoYTiendasEnPeriodo` en rango semiabierto `[desdeInclusive, hastaExclusivo)`.
- **OpenAPI:** actualizada la descripción y respuestas (`200`, `400`, `403`, `404`) en `PatronesDemandaController`.
- **Cobertura de pruebas:**
  - Ampliado `ConsultarPatronesDemandaServiceTest` con casos de deduplicación, validaciones de entrada, `LocalDate.MAX`, `404` y rechazo `403` cuando el usuario no es `PLANIFICADOR`.
  - Agregada la suite de integración `PatronesDemandaIntegrationTest` (`MockMvc` + `jwt()` sobre H2).

---

## 3. Archivos Principales

| Archivo | Cambio |
|---|---|
| `application/service/ConsultarPatronesDemandaService.java` | Límites de rango/tiendas, deduplicación y consulta batch |
| `infrastructure/adapter/in/rest/PatronesDemandaController.java` | Documentación OpenAPI de límites y respuestas |
| `src/test/java/.../ConsultarPatronesDemandaServiceTest.java` | Pruebas unitarias de validación, bordes y autorización |
| `src/test/java/.../PatronesDemandaIntegrationTest.java` | Pruebas HTTP end-to-end con `MockMvc` y H2 |

---

## 4. Verificación

- `./mvnw.cmd test` ejecutado tras añadir las pruebas: suite completa en verde (`BUILD SUCCESS`, 0 fallos, 0 errores).
