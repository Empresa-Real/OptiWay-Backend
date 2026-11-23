# HU-19 — Consultar patrones de demanda por tienda y producto

**Épica 5:** Análisis y reportes  
**Feature:** Análisis de demanda y rotación  
**Sprint:** 3  
**Estado:** ✅ **Implementado y mergeado en `main`** (PR #21)

---

## Historia de Usuario

> **Como** Analista/Planificador de cadena de suministro  
> **Quiero** ver el histórico de ventas de cada producto por tienda  
> **Para** identificar qué se vende más y en qué temporadas.

Fuente: `product_backlog.txt:391-401`.

---

## Criterios de Aceptación (backlog)

1. Puedo consultar la evolución de ventas de un producto en una tienda específica dentro de un rango de fechas.
2. Puedo comparar la demanda de un mismo producto entre varias tiendas seleccionadas.
3. El sistema resalta visualmente cuáles tiendas tienen mayor y cuál menor rotación de ese producto.

---

## Estado de Implementación

### Endpoint real

```http
GET /api/demanda/patrones?productoId=3&tiendaIds=1&tiendaIds=2&desde=2026-11-01&hasta=2026-11-03
```

`tiendaIds` admite tanto valores repetidos (`tiendaIds=1&tiendaIds=2`) como separados por coma (`tiendaIds=1,2`). Las fechas usan ISO-8601 (`yyyy-MM-dd`).

### Respuesta

```json
{
  "productoId": 3,
  "desde": "2026-11-01",
  "hasta": "2026-11-03",
  "tiendas": [
    {
      "tiendaId": 1,
      "unidadesVendidas": 9,
      "evolucion": [
        { "fecha": "2026-11-01", "unidadesVendidas": 5 },
        { "fecha": "2026-11-02", "unidadesVendidas": 0 },
        { "fecha": "2026-11-03", "unidadesVendidas": 4 }
      ]
    }
  ],
  "tiendasMayorRotacion": [1],
  "tiendasMenorRotacion": [2]
}
```

### Reglas vigentes

* **Rol:** solo `PLANIFICADOR`. Un `ADMINISTRADOR` recibe `403` (`requirePlanificador` no lo incluye), coherente con HU-20/HU-21.
* **Límites:** máximo **366 días** por rango y **50 tiendas distintas** por consulta. Los IDs repetidos se deduplican antes de contar.
* **Rango:** ambos extremos inclusive; se consulta `[desde 00:00, hasta+1 00:00)` y se agrupa por día. Los días sin ventas se devuelven con `0`.
* **Rotación:** `tiendasMayorRotacion` y `tiendasMenorRotacion` contienen **todos** los IDs empatados en el máximo/mínimo.
* **Consulta eficiente:** la existencia de tiendas se valida en una sola llamada batch (`obtenerPorIds`), no una por tienda.
* **Filtros:** solo cuentan ventas del `productoId` y de las tiendas seleccionadas; se ignoran las de otros productos/tiendas aunque caigan en el rango.

### Errores

| Código | Cuándo |
| :--- | :--- |
| `400` | Parámetros nulos/inválidos, fechas invertidas, rango > 366 días o > 50 tiendas. |
| `401` | Falta JWT o el claim `sub` no es un UUID válido. |
| `403` | El usuario autenticado no es `PLANIFICADOR`. |
| `404` | No existe el producto o alguna de las tiendas solicitadas. |

La fuente autoritativa del contrato es `https://optiway-backend.onrender.com/v3/api-docs`. El endpoint también está listado en [`docs/Swagger_OpenAPI.md`](../../Swagger_OpenAPI.md).

---

## Componentes Hexagonales Asociados

| Capa | Archivo |
| :--- | :--- |
| Puerto de entrada | `src/main/java/com/example/optiway/application/port/in/ConsultarPatronesDemandaUseCase.java` |
| DTO de respuesta | `src/main/java/com/example/optiway/application/port/in/dto/PatronesDemandaResponse.java` |
| Servicio | `src/main/java/com/example/optiway/application/service/ConsultarPatronesDemandaService.java` |
| Puerto de salida | `VentaRepositoryPort`, `TiendaRepositoryPort.obtenerPorIds(...)` |
| Adaptador REST | `src/main/java/com/example/optiway/infrastructure/adapter/in/rest/PatronesDemandaController.java` |
| Persistencia | `VentaJpaRepository.buscarVentasPorProductoYTiendasEnPeriodo(...)`, `VentaRepositoryAdapter`, `TiendaRepositoryAdapter` |
| Acción autorizada | `AuthorizedAction.CONSULTAR_PATRONES_DEMANDA` |

---

## Pruebas

* `ConsultarPatronesDemandaServiceTest`: 12 pruebas unitarias (agregación por día, ceros, empates, deduplicación, validaciones, 404 y RBAC).
* `PatronesDemandaIntegrationTest`: 7 pruebas MVC con JWT real sobre H2. Cubren bindings repetido y por coma, formato de fecha, límites de rango/tiendas, `403`, `404` y el intervalo `[desde, hasta]` con ruido de otros productos/tiendas.

---

## Pendiente para el frontend

El resaltado visual de `tiendasMayorRotacion` / `tiendasMenorRotacion` debe hacerse en la pantalla; el backend solo entrega las listas de IDs. No existe aún una pantalla propia documentada para HU-19.
