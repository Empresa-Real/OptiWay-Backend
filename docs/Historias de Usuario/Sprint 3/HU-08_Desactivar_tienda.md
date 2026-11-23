# HU-08 — Desactivar tienda

**Épica 2:** Catálogo y red de distribución  
**Feature:** Gestión de tiendas y centros de distribución  
**Sprint:** 3  
**Estado:** Implementado (merge `feature/hu-08-hu-15-tiendas-reposicion`)

---

## Historia de Usuario

> **Como** Administrador del sistema  
> **Quiero** desactivar una tienda  
> **Para** mantener actualizada la plataforma cuando esa tienda cierra o deja de operar.

Fuente: `product_backlog.txt:181-189`.

---

## Criterios de Aceptación

1. Al desactivar una tienda, deja de recibir nuevas asignaciones de usuarios o inventario, y su historial se conserva.
2. Si la tienda tiene una orden de reposición en tránsito, el sistema advierte antes de desactivar y permite confirmar o cancelar.

**Implementación:** ambos criterios están cubiertos. El criterio 2 se resuelve con un endpoint auxiliar de consulta y un flag de confirmación en el body.

---

## Especificación de Endpoints

### 1. Cambiar estado de la tienda

* **Método:** `PATCH`
* **Ruta:** `/api/tiendas/{id}/estado`
* **Seguridad:** `Authorization: Bearer <token_jwt_supabase>`, rol `ADMINISTRADOR`.
* **Path param:** `id` (`Long`) — ID numérico de la tienda.
* **Cuerpo de la petición (JSON, obligatorio):**

```json
{
  "activo": false,
  "confirmarConOrdenesEnTransito": true
}
```

| Campo | Tipo | Obligatorio | Descripción |
| :--- | :--- | :--- | :--- |
| `activo` | `Boolean` | **Sí** (`@NotNull`) | `false` desactiva; `true` reactiva. |
| `confirmarConOrdenesEnTransito` | `Boolean` | No | Debe ser `true` para desactivar si existen órdenes en tránsito/enviadas. |

* Éxito: `200 OK` (no `201` ni `204`).

### 2. Verificar órdenes en tránsito (soporte del modal)

* **Método:** `GET`
* **Ruta:** `/api/tiendas/{id}/ordenes-en-transito`
* **Respuesta (`200 OK`):**

```json
{
  "tieneOrdenesEnTransito": true
}
```

> No existe un endpoint análogo para CD (HU-10): el frontend obtiene las tiendas asignadas del propio `CentroDistribucionResponse`.

---

## Respuestas Esperadas

| Código HTTP | Condición | Cuerpo de Respuesta |
| :--- | :--- | :--- |
| **`200 OK`** | Estado modificado. | `TiendaResponse`: `id, codigo, nombre, direccion, ciudad, activo, estado ("Activa"/"Inactiva"), encargadoId, centrosAbastecedoresIds`. |
| **`200 OK`** | Consulta auxiliar. | `{"tieneOrdenesEnTransito": true\|false}`. |
| **`400 Bad Request`** | `activo` ausente o nulo. | `{"error": "El campo activo es obligatorio", "message": "El campo activo es obligatorio"}` |
| **`400 Bad Request`** | `id` no numérico. | `{"error": "El valor '...' del parámetro 'id' no es válido. Se esperaba un número entero válido.", "message": "..."}` |
| **`401 Unauthorized`** | Token no suministrado o inválido. | `{"error": "Autenticación requerida. Token no proporcionado o inválido.", "message": "..."}` |
| **`403 Forbidden`** | Usuario sin rol `ADMINISTRADOR`. | `{"error": "Solo un administrador puede activar o desactivar tiendas", "message": "..."}` |
| **`404 Not Found`** | Tienda inexistente. | `{"error": "Tienda no encontrada con ID: 15", "message": "..."}` |
| **`409 Conflict`** | Órdenes en tránsito y flag de confirmación ausente/`false`. | `{"error": "La tienda tiene órdenes de reposición en tránsito o enviadas. Se requiere confirmación.", "message": "..."}` |
| **`500`** | Error no controlado. | `{"error": "Ocurrió un error interno en el servidor. Por favor intente más tarde.", "message": "..."}` |

> Formato real de error: `ErrorResponse` duplica el mensaje en `error` y `message` (`code` se omite cuando es `null`). Las guías anteriores mostraban solo `error` de forma simplificada.

---

## Reglas de Negocio

* La advertencia solo se evalúa al **desactivar** (`activo = false`); reactivar omite la comprobación.
* Se consideran órdenes en tránsito los estados `EN_TRANSITO`, `ENVIADO` y `ENVIADA` (`OrdenReposicionRepositoryAdapter.java:68-69`).
* La confirmación debe ser estrictamente `true`; omitirla o enviar `false` mantiene el `409`.
* Desactivar una tienda ya inactiva es idempotente: devuelve `200` (no hay validación de "ya inactiva").
* No se borra ningún registro: el historial de ventas, inventarios y asignaciones se conserva; solo cambia `tiendas.activo`.
* Efectos aguas abajo de una tienda inactiva:
  * Asignar encargado → `400 "No se puede operar sobre una tienda inactiva."`
  * Registrar venta → `400 "La tienda con ID X se encuentra inactiva y no puede registrar ventas."`
  * Solicitar reposición (HU-15) → `409 "No se puede solicitar reposición para una tienda inactiva."`
  * Recomendaciones (HU-20/21) → `400 "No se puede operar con una tienda inactiva."`
* **Matiz de integración:** las órdenes recién creadas por HU-21 tienen estado `APROBADA`, que **no** está en la lista de estados advertidos. La advertencia aparece cuando la orden pasa a `ENVIADO`/`EN_TRANSITO` (HU-22/23, fuera de este sprint).

---

## Componentes Hexagonales Asociados

* **Controlador REST:** [`TiendaController.java`](../../../src/main/java/com/example/optiway/infrastructure/adapter/in/rest/TiendaController.java) (`cambiarEstadoTienda`, `verificarOrdenesEnTransito`)
* **Puerto de entrada:** [`CambiarEstadoTiendaUseCase.java`](../../../src/main/java/com/example/optiway/application/port/in/CambiarEstadoTiendaUseCase.java)
* **Servicio de aplicación:** [`TiendaService.java`](../../../src/main/java/com/example/optiway/application/service/TiendaService.java) (`cambiarEstado`, `tieneOrdenesEnTransitoOEnviadas`)
* **Autorización:** [`AuthorizationService.java`](../../../src/main/java/com/example/optiway/infrastructure/security/AuthorizationService.java) (`requireAdministrador`) · [`AuthorizedAction.CAMBIAR_ESTADO_TIENDA`](../../../src/main/java/com/example/optiway/infrastructure/security/AuthorizedAction.java)
* **DTO request:** [`CambiarEstadoTiendaRequest.java`](../../../src/main/java/com/example/optiway/infrastructure/adapter/in/rest/dto/CambiarEstadoTiendaRequest.java)
* **DTO response:** [`TiendaResponse.java`](../../../src/main/java/com/example/optiway/application/port/in/dto/TiendaResponse.java) · [`OrdenesEnTransitoResponse.java`](../../../src/main/java/com/example/optiway/application/port/in/dto/OrdenesEnTransitoResponse.java)
* **Puertos de salida:** `TiendaRepositoryPort`, `OrdenReposicionRepositoryPort`
* **Adaptadores de persistencia:** `TiendaRepositoryAdapter`, `OrdenReposicionRepositoryAdapter`
* **Entidades:** `TiendaJpaEntity` (`tiendas.activo`), `OrdenReposicionJpaEntity` (`ordenes_reposicion.estado`)

---

## Flujo en Arquitectura Hexagonal

```text
Cliente (Administrador)
  ↓ PATCH /api/tiendas/{id}/estado  (o GET /{id}/ordenes-en-transito) + Bearer JWT
SecurityFilterChain → @AuthUserId (UUID del claim 'sub')
  ↓
TiendaController
  ↓ CambiarEstadoTiendaRequest (activo, confirmarConOrdenesEnTransito)
CambiarEstadoTiendaUseCase
  ↓
TiendaService.cambiarEstado
  ├── AuthorizationService.requireAdministrador → 403 si no es ADMINISTRADOR
  ├── OrdenReposicionRepositoryPort.existeOrdenEnTransitoOEnviadaPorTienda
  │      → OrdenReposicionRepositoryAdapter → ordenes_reposicion (EN_TRANSITO/ENVIADO/ENVIADA)
  ├── 409 si hay órdenes y no se confirmó
  └── TiendaRepositoryPort.save → tiendas.activo
  ↓
TiendaResponse
```

---

## Pruebas y Migraciones

* **Tests directos:** no existen para `cambiarEstado` ni para el endpoint auxiliar. Cobertura indirecta en `MatrizPermisosTest` (crear/listar tiendas), `AuthorizationServiceTest` (`requireAdministrador`) y `ApiExceptionHandlerTest` (path param no numérico).
* **Migración relacionada:** [`docs/migrations/fix_tiendas_estado_a_activo.sql`](../../migrations/fix_tiendas_estado_a_activo.sql) convierte el antiguo `estado` varchar a `activo boolean NOT NULL DEFAULT true`.
* **Sin `@Transactional`** en el servicio: la única escritura es el `save` del repositorio.
