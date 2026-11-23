# HU-10 — Desactivar centro de distribución

**Épica 2:** Catálogo y red de distribución  
**Feature:** Gestión de tiendas y centros de distribución  
**Sprint:** 3  
**Estado:** Implementado (integrado desde `origin/HU-10-Desactivar-CD`)

---

## Historia de Usuario

> **Como** Administrador del sistema  
> **Quiero** desactivar un centro de distribución  
> **Para** reflejar que ya no está operativo y evitar que se le sigan asignando tiendas o recomendaciones de reposición.

Fuente: `product_backlog.txt:203-211`.

---

## Criterios de Aceptación

1. Al desactivar un CD, deja de poder recibir tiendas asignadas o nuevas recomendaciones, y su historial se conserva.
2. Si el CD tiene tiendas asignadas, el sistema advierte antes de desactivar y permite confirmar o cancelar.

**Implementación:** ambos criterios están cubiertos. No existe un endpoint auxiliar de consulta de tiendas asignadas: el frontend obtiene esa información de `CentroDistribucionResponse.tiendasAbastecidasIds` o de `GET /api/centros-distribucion`.

---

## Especificación del Endpoint

* **Método:** `PATCH`
* **Rutas soportadas:** `/api/centros-distribucion/{id}/estado` y el alias `/api/cd/{id}/estado`
* **Seguridad:** `Authorization: Bearer <token_jwt_supabase>`, rol `ADMINISTRADOR`.
* **Path param:** `id` (`Long`) — ID numérico del centro de distribución.
* **Cuerpo de la petición (JSON, obligatorio):**

```json
{
  "activo": false,
  "confirmarConTiendasAsignadas": true
}
```

| Campo | Tipo | Obligatorio | Descripción |
| :--- | :--- | :--- | :--- |
| `activo` | `Boolean` | **Sí** (`@NotNull`) | `false` desactiva; `true` reactiva. |
| `confirmarConTiendasAsignadas` | `Boolean` | No | Debe ser `true` si el CD abastece tiendas. |

* Éxito: `200 OK`.

---

## Respuestas Esperadas

| Código HTTP | Condición | Cuerpo de Respuesta |
| :--- | :--- | :--- |
| **`200 OK`** | Estado modificado. | `CentroDistribucionResponse`: `id, codigo, nombre, direccion, capacidad, encargadoId, tiendasAbastecidasIds, estado ("Activo"/"Inactivo")`. |
| **`400 Bad Request`** | `activo` ausente o nulo. | `{"error": "El campo activo es obligatorio", "message": "..."}` |
| **`400 Bad Request`** | `id` no numérico / JSON inválido. | `{"error": "El valor '...' del parámetro 'id' no es válido. Se esperaba un número entero válido.", "message": "..."}` |
| **`401 Unauthorized`** | Token no suministrado o inválido. | `{"error": "Autenticación requerida. Token no proporcionado o inválido.", "message": "..."}` |
| **`403 Forbidden`** | Usuario sin rol `ADMINISTRADOR`. | `{"error": "Solo un administrador puede cambiar el estado de Centros de Distribución", "message": "..."}` |
| **`404 Not Found`** | CD inexistente. | `{"error": "Centro de distribución no encontrado con ID: 20", "message": "..."}` |
| **`409 Conflict`** | CD con tiendas asignadas sin confirmar. | `{"error": "El centro tiene tiendas asignadas. Confirme la desactivación con confirmarConTiendasAsignadas=true.", "message": "..."}` |
| **`500`** | Error no controlado. | `{"error": "Ocurrió un error interno en el servidor. Por favor intente más tarde.", "message": "..."}` |

---

## Reglas de Negocio

* La advertencia solo se evalúa al desactivar. Se comprueba `tiendasAbastecidasIds != null && !isEmpty()`.
* La confirmación debe ser estrictamente `true` (`Boolean.TRUE.equals` en el controlador).
* **No se comprueban órdenes en tránsito para CDs** (esa regla es exclusiva de HU-08).
* Desactivar un CD ya inactivo es idempotente: devuelve `200`.
* El estado se persiste como **String** (`"Activo"` / `"Inactivo"`), no como booleano; `isActivo()` se deriva de `!"Inactivo".equalsIgnoreCase(estado)`.
* No se eliminan relaciones ni historial: se actualiza `centros_distribucion.estado` y se conserva la tabla intermedia `centro_distribucion_tiendas`.
* Efectos aguas abajo de un CD inactivo:
  * Registrar ingreso de mercancía → `400 "El centro de distribución con ID X se encuentra inactivo y no puede recibir mercancía."`
  * Crear/aprobar recomendaciones (HU-20/21) → `400 "No se puede operar con un Centro de Distribución inactivo."`
* El alias `/api/cd` expone **todas** las operaciones del controlador, no solo el cambio de estado.

---

## Componentes Hexagonales Asociados

* **Controlador REST:** [`CentroDistribucionController.java`](../../../src/main/java/com/example/optiway/infrastructure/adapter/in/rest/CentroDistribucionController.java) (`cambiarEstado`)
* **Puerto de entrada:** `CambiarEstadoCentroDistribucionUseCase.java`
* **Servicio de aplicación:** `CentroDistribucionService.java` (`cambiarEstado`)
* **Autorización:** `AuthorizationService.requireAdministrador` · `AuthorizedAction.CAMBIAR_ESTADO_CENTRO_DISTRIBUCION`
* **DTO request:** [`CambiarEstadoCDRequest.java`](../../../src/main/java/com/example/optiway/infrastructure/adapter/in/rest/dto/CambiarEstadoCDRequest.java) (record)
* **DTO response:** `CentroDistribucionResponse.java`
* **Puerto de salida:** `CentroDistribucionRepositoryPort`
* **Adaptador de persistencia:** `CentroDistribucionRepositoryAdapter` → `CentroDistribucionJpaRepository` / `TiendaJpaRepository`
* **Entidades:** `CentroDistribucionJpaEntity` (`centros_distribucion.estado`), `centro_distribucion_tiendas`

---

## Flujo en Arquitectura Hexagonal

```text
Cliente (Administrador)
  ↓ PATCH /api/centros-distribucion/{id}/estado  (o /api/cd/{id}/estado) + Bearer JWT
SecurityFilterChain → @AuthUserId (UUID del claim 'sub')
  ↓
CentroDistribucionController.cambiarEstado
  ↓ CambiarEstadoCDRequest → boolean activo, boolean confirmarConTiendasAsignadas
CambiarEstadoCentroDistribucionUseCase
  ↓
CentroDistribucionService.cambiarEstado
  ├── AuthorizationService.requireAdministrador → 403 si no es ADMINISTRADOR
  ├── 404 si el CD no existe
  ├── 409 si hay tiendas asignadas y no se confirmó
  └── CentroDistribucionRepositoryPort.guardar → centros_distribucion.estado
  ↓
CentroDistribucionResponse
```

---

## Pruebas y Migraciones

* **Tests directos:** no existen para el cambio de estado. Cobertura indirecta en `MatrizPermisosTest` (creación de CD), `AuthorizationServiceTest` (`requireAdministrador`) y `RecomendacionReposicionServiceTest` (rechazo por CD inactivo).
* **Migración SQL:** no hay ninguna migración que cree o modifique `centros_distribucion.estado`; proviene de `spring.jpa.hibernate.ddl-auto=update`.
* **Sin `@Transactional`** en el servicio: la única escritura es el `save` del repositorio.
