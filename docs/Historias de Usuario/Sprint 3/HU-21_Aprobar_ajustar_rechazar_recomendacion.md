# HU-21 — Aprobar, ajustar o rechazar una recomendación de reposición

**Épica 4:** Reposición y seguimiento de órdenes  
**Feature:** Recomendaciones de reposición  
**Sprint:** 3  
**Estado:** Implementado (merge `feature/HU-21`, PR #16)

---

## Historia de Usuario

> **Como** Analista/Planificador de cadena de suministro  
> **Quiero** aprobar, ajustar o rechazar una recomendación de reposición antes de que se convierta en una orden  
> **Para** mantener control sobre los traslados de mercancía.

Fuente: `product_backlog.txt:325-337`.

---

## Criterios de Aceptación

1. Al aprobar una recomendación en estado "Propuesta", el sistema la convierte en orden de reposición "Aprobada", visible para el CD de origen.
2. Puedo modificar la cantidad o el CD de origen antes de aprobar; la orden generada refleja esos valores ajustados.
3. Al rechazar una recomendación indicando un motivo, el sistema la marca como rechazada y no genera ninguna orden.
4. Queda registro de quién aprobó, ajustó o rechazó la recomendación y cuándo.

**Implementación:** criterios 2, 3 y 4 cubiertos. Del criterio 1 se implementa la generación de la orden con estado `APROBADA`; **no existe aún un endpoint de consulta de órdenes para el CD de origen** (HU-22/23, fuera de este sprint). El booleano `GET /api/tiendas/{id}/ordenes-en-transito` solo consulta estados `EN_TRANSITO`, `ENVIADO` y `ENVIADA`.

---

## Especificación de Endpoints

### 1. Aprobar (o aprobar con ajustes)

* **Método:** `PATCH`
* **Ruta:** `/api/recomendaciones-reposicion/{id}/aprobar`
* **Seguridad:** Bearer JWT; **solo rol `PLANIFICADOR`** (ADMINISTRADOR excluido).
* **Path param:** `id` (`Long`) — ID de la recomendación.
* **Cuerpo:** **opcional**. Sin body, se aprueba sin ajustes.

```json
{
  "cantidadAjustada": 40,
  "centroDistribucionOrigenId": 25,
  "confirmarFueraDeCobertura": false
}
```

| Campo | Tipo | Validación |
| :--- | :--- | :--- |
| `cantidadAjustada` | `Integer` | `@Positive("La cantidad ajustada debe ser un número entero positivo.")` |
| `centroDistribucionOrigenId` | `Long` | `@Positive("El ID del Centro de Distribución de origen debe ser positivo.")` |
| `confirmarFueraDeCobertura` | `Boolean` | Opcional; requerido en `true` solo si se cambia el CD a uno fuera de cobertura |

* Éxito: `200 OK` con `OrdenReposicionResponse` (`estado: "APROBADA"`).

### 2. Rechazar

* **Método:** `PATCH`
* **Ruta:** `/api/recomendaciones-reposicion/{id}/rechazar`
* **Cuerpo (obligatorio):**

```json
{
  "motivo": "Sobre-stock en tienda"
}
```

| Campo | Tipo | Validación |
| :--- | :--- | :--- |
| `motivo` | `String` | `@NotBlank("El motivo de rechazo es obligatorio.")`, `@Size(max = 500, "El motivo de rechazo no puede superar los 500 caracteres.")` |

* Éxito: `200 OK` con `RecomendacionReposicionResponse` (`estado: "RECHAZADA"`), **sin orden generada**.

### 3. Consultar recomendaciones

* `GET /api/recomendaciones-reposicion` → `200 OK` (heredado de HU-20).

---

## Respuestas Esperadas

| Código HTTP | Condición | Cuerpo de Respuesta |
| :--- | :--- | :--- |
| **`200 OK`** (aprobar) | Orden generada. | `OrdenReposicionResponse`: `id, recomendacionId, productoId, nombreProducto, cantidad, centroDistribucionOrigenId, tiendaDestinoId, estado ("APROBADA"), creadoPor, fechaCreacion`. |
| **`200 OK`** (rechazar) | Recomendación rechazada. | `RecomendacionReposicionResponse` con `estado = "RECHAZADA"`, `motivoRechazo`, `accionTomada = "RECHAZADA"`, `resueltoPorUsuarioId`, `fechaResolucion`. |
| **`400 Bad Request`** | Datos inválidos. | `El ID de la recomendación debe ser válido.` · `La cantidad ajustada debe ser mayor que cero.` · `El ID del Centro de Distribución debe ser válido.` · `El motivo de rechazo es obligatorio.` · `El motivo de rechazo no puede superar los 500 caracteres.` · mensajes `@Positive` del DTO de aprobación. |
| **`400 Bad Request`** | Entidades inactivas o stock insuficiente tras el ajuste. | `No se puede operar con un producto inactivo.` · `No se puede operar con una tienda inactiva.` · `No se puede operar con un Centro de Distribución inactivo.` · `Stock insuficiente en el Centro de Distribución. Disponible: X. Cantidad solicitada: Y.` |
| **`401 Unauthorized`** | Token no suministrado o inválido. | `{"error": "Autenticación requerida. Token no proporcionado o inválido.", "message": "..."}` |
| **`403 Forbidden`** | Rol distinto de PLANIFICADOR. | `Solo un PLANIFICADOR puede aprobar recomendaciones de reposición` · `Solo un PLANIFICADOR puede rechazar recomendaciones de reposición` |
| **`404 Not Found`** | Recomendación/entidades inexistentes. | `No se encontró la recomendación de reposición con ID: X` · `No se encontró el producto indicado.` · `No se encontró la tienda de destino.` · `No se encontró el Centro de Distribución de origen.` |
| **`409 Conflict`** | Recomendación ya resuelta. | `{"error": "La recomendación ya fue resuelta con estado: APROBADA", "message": "..."}` |
| **`409 Conflict`** | Nuevo CD fuera de cobertura sin confirmar. | `{"error": "La tienda no pertenece a la zona de cobertura del nuevo Centro de Distribución seleccionado. Confirme la operación enviando confirmarFueraDeCobertura: true o seleccione otro Centro de Distribución.", "message": "..."}` |
| **`500`** | Error no controlado. | `{"error": "Ocurrió un error interno en el servidor. Por favor intente más tarde.", "message": "..."}` |

> No se devuelve `201` en HU-21. Formato real de error: `{"error":"...","message":"..."}`.

---

## Reglas de Negocio

* **Estados literales:** `PROPUESTA`, `APROBADA`, `RECHAZADA` (no hay `enum`). Solo se puede resolver una recomendación en estado `PROPUESTA`.
* **Concurrencia:** la recomendación se carga con `PESSIMISTIC_WRITE` (`findByIdForUpdate`) antes de cualquier escritura.
* **Ajustes:**
  * Si `cantidadAjustada` difiere de la original, se guarda el valor original en `cantidadPropuesta` (solo si estaba vacío) y se sobrescribe `cantidad`.
  * Si `centroDistribucionOrigenId` difiere, se guarda el CD original en `centroDistribucionPropuestoId` y se reemplaza el origen.
  * Después del ajuste se revalidan producto/tienda activos, CD activo, stock con la cantidad final y cobertura. Si el CD cambió y queda fuera de cobertura, se exige `confirmarFueraDeCobertura: true`.
  * `accionTomada` = `AJUSTADA_Y_APROBADA` si hubo ajuste, o `APROBADA` en caso contrario.
* **Orden generada:** `estado = "APROBADA"`, relación 1:1 con la recomendación (`recomendacion_id` `UNIQUE`), `creadoPor = authUserId` del planificador, `fechaCreacion = fechaResolucion`.
* **Stock:** se valida, **no se descuenta**. El descuento corresponde a HU-22/23 (fuera de este sprint).
* **Rechazo:** motivo obligatorio, se hace `trim()` y se limita a 500 caracteres; no genera orden.
* **Trazabilidad:** `resueltoPorUsuarioId`, `fechaResolucion`, `accionTomada` y `motivoRechazo` quedan registrados en la recomendación.
* **Atomicidad:** si falla la persistencia de la orden, se revierte también la actualización de la recomendación (`@Transactional` en el servicio y en los adaptadores).
* **Auditoría y borrado de usuarios:** FK `resuelto_por` y `creado_por` con `ON DELETE SET NULL`; ver migración abajo.

---

## Componentes Hexagonales Asociados

* **Controlador REST:** [`RecomendacionReposicionController.java`](../../../src/main/java/com/example/optiway/infrastructure/adapter/in/rest/RecomendacionReposicionController.java) (`aprobar`, `rechazar`)
* **Puertos de entrada:** `AprobarRecomendacionUseCase.java`, `RechazarRecomendacionUseCase.java` y sus `Command`
* **Servicio de aplicación:** [`RecomendacionReposicionService.java`](../../../src/main/java/com/example/optiway/application/service/RecomendacionReposicionService.java) (`@Transactional`)
* **Autorización:** `AuthorizationService.requirePlanificador` · `AuthorizedAction.APROBAR_RECOMENDACION_REPOSICION` / `RECHAZAR_RECOMENDACION_REPOSICION`
* **DTOs request:** [`AprobarRecomendacionRequest.java`](../../../src/main/java/com/example/optiway/infrastructure/adapter/in/rest/dto/AprobarRecomendacionRequest.java) · [`RechazarRecomendacionRequest.java`](../../../src/main/java/com/example/optiway/infrastructure/adapter/in/rest/dto/RechazarRecomendacionRequest.java)
* **DTOs response:** `OrdenReposicionResponse.java` · `RecomendacionReposicionResponse.java`
* **Dominio:** `RecomendacionReposicion.java` · `OrdenReposicion.java`
* **Puertos de salida:** `RecomendacionReposicionRepositoryPort` (`obtenerPorIdParaActualizar`), `OrdenReposicionRepositoryPort`, puertos de producto/tienda/CD/inventario
* **Repositorios JPA:** `RecomendacionReposicionJpaRepository.findByIdForUpdate` (`@Lock(PESSIMISTIC_WRITE)`), `OrdenReposicionJpaRepository`
* **Entidades:** `RecomendacionReposicionJpaEntity`, `OrdenReposicionJpaEntity` (`ordenes_reposicion`)

---

## Flujo en Arquitectura Hexagonal

```text
Cliente (Planificador)
  ↓ PATCH /api/recomendaciones-reposicion/{id}/aprobar  (o /rechazar) + Bearer JWT
SecurityFilterChain → @AuthUserId
  ↓
RecomendacionReposicionController → Aprobar/RechazarRecomendacionRequest.toCommand(id)
  ↓
Aprobar/RechazarRecomendacionUseCase
  ↓
RecomendacionReposicionService  [@Transactional]
  ├── requirePlanificador → 403 para ADMIN u otros roles
  ├── obtenerPorIdParaActualizar → PESSIMISTIC_WRITE → 404 / 409 si ya resuelta
  ├── (aprobar) aplicar ajustes → revalidar stock/cobertura → guardar recomendación
  │      └── OrdenReposicionRepositoryPort.guardar → estado APROBADA
  └── (rechazar) validar motivo → estado RECHAZADA → sin orden
  ↓
OrdenReposicionResponse (200) / RecomendacionReposicionResponse (200)
```

---

## Pruebas y Migración SQL

* **Tests unitarios:** [`RecomendacionReposicionServiceTest.java`](../../../src/test/java/com/example/optiway/RecomendacionReposicionServiceTest.java) — aprobación sin ajustes; stock insuficiente; entidades inactivas; ajuste de cantidad y CD con trazabilidad de valores originales; nuevo CD fuera de cobertura sin confirmar; estado distinto de `PROPUESTA`; ID inexistente; rechazo con motivo (`trim()`) y sin orden; motivo vacío o > 500.
* **Matriz RBAC:** [`MatrizPermisosTest.java`](../../../src/test/java/com/example/optiway/MatrizPermisosTest.java) (`RecomendacionesReposicion`) — PLANIFICADOR aprueba y rechaza; ADMINISTRADOR, ENCARGADO_TIENDA, ENCARGADO_CD y ausencia de identidad reciben `AccesoDenegadoException`.
* **Test de integración:** [`RecomendacionReposicionRollbackIntegrationTest.java`](../../../src/test/java/com/example/optiway/RecomendacionReposicionRollbackIntegrationTest.java) — si falla la persistencia de la orden, la recomendación permanece `PROPUESTA` con cantidad original y sin campos de resolución; no queda orden.
* **Migración:** [`docs/migrations/hu21_ordenes_y_auditoria_recomendaciones.sql`](../../migrations/hu21_ordenes_y_auditoria_recomendaciones.sql), idempotente, necesaria porque `ddl-auto=update` no recrea restricciones ni relaja `NOT NULL`:
  1. `ALTER COLUMN creado_por DROP NOT NULL` en `ordenes_reposicion`.
  2. Añade `cantidad_propuesta` y `centro_distribucion_propuesto_id` a `recomendaciones_reposicion` (columnas de trazabilidad del ajuste).
  3. Limpia referencias huérfanas y recrea `fk_recomendaciones_resuelto_por` con `ON DELETE SET NULL`.
  4. Limpia referencias huérfanas y recrea `fk_ordenes_creador` con `ON DELETE SET NULL`.
  * No crea las tablas ni el resto de columnas: esas provienen del mapeo JPA.
