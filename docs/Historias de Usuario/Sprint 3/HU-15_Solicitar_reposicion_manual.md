# HU-15 — Solicitar reposición manual

**Épica 4:** Reposición y seguimiento de órdenes  
**Feature:** Recepción y reposición desde tienda  
**Sprint:** 3  
**Estado:** Implementado (merge `feature/hu-08-hu-15-tiendas-reposicion`)

---

## Historia de Usuario

> **Como** Encargado de tienda  
> **Quiero** solicitar reposición de un producto cuando note que se está agotando  
> **Para** anticiparme antes de perder ventas.

Fuente: `product_backlog.txt:301-309`.

---

## Criterios de Aceptación

1. Al solicitar reposición indicando producto y cantidad, el sistema registra la solicitud como "pendiente" y la deja visible para el Planificador.
2. El sistema exige indicar la cantidad deseada; si falta, muestra error y no registra la solicitud.

**Implementación:** ambos criterios cubiertos. El estado persistido es `PENDIENTE` (mayúsculas) y la visibilidad se resuelve con `GET /api/solicitudes-reposicion`.

---

## Especificación de Endpoints

### 1. Crear solicitud de reposición

* **Método:** `POST`
* **Ruta:** `/api/tiendas/{id}/solicitudes-reposicion`
* **Seguridad:** `Authorization: Bearer <token_jwt_supabase>`; solo `ENCARGADO_TIENDA` asignado a la tienda.
* **Path param:** `id` (`Long`) — ID de la tienda.
* **Cuerpo de la petición (JSON, obligatorio):**

```json
{
  "productoId": 10,
  "cantidad": 15
}
```

| Campo | Tipo | Validación |
| :--- | :--- | :--- |
| `productoId` | `Long` | `@NotNull("El ID del producto es obligatorio")` |
| `cantidad` | `Integer` | `@NotNull("La cantidad es obligatoria")`, `@Min(1, "La cantidad debe ser mayor o igual a 1")` |

* Éxito: `201 Created` con `SolicitudReposicionResponse`.

### 2. Listar solicitudes (para el Planificador)

* **Método:** `GET`
* **Ruta:** `/api/solicitudes-reposicion`
* **Sin parámetros.**
* **Éxito:** `200 OK` con `List<SolicitudReposicionResponse>`.
* **Rol:** solo `PLANIFICADOR` (un ADMINISTRADOR recibe `403`).

---

## Respuestas Esperadas

| Código HTTP | Condición | Cuerpo de Respuesta |
| :--- | :--- | :--- |
| **`201 Created`** | Solicitud registrada. | `SolicitudReposicionResponse`: `id, tiendaId, productoId, nombreProducto, cantidad, estado ("PENDIENTE"), usuarioId, fechaSolicitud`. |
| **`200 OK`** | Listado del planificador. | Array de `SolicitudReposicionResponse`. |
| **`400 Bad Request`** | `productoId`/`cantidad` nulos o `cantidad < 1`. | `El ID del producto es obligatorio` · `La cantidad es obligatoria` · `La cantidad debe ser mayor o igual a 1` · `productoId y cantidad (>= 1) son obligatorios.` |
| **`401 Unauthorized`** | Token no suministrado o inválido. | `{"error": "Autenticación requerida. Token no proporcionado o inválido.", "message": "..."}` |
| **`403 Forbidden`** | Sin permiso sobre la tienda / rol incorrecto. | `No tiene permisos para solicitar reposición manual de inventario.` · `Solo un encargado de tienda puede solicitar reposición manual.` · `Solo un planificador puede consultar las solicitudes de reposición.` |
| **`404 Not Found`** | Tienda o producto inexistente. | `Tienda no encontrada con ID: X` · `Producto no encontrado con ID: X` |
| **`409 Conflict`** | Tienda inactiva (HU-08). | `{"error": "No se puede solicitar reposición para una tienda inactiva.", "message": "..."}` |
| **`500`** | Error no controlado. | `{"error": "Ocurrió un error interno en el servidor. Por favor intente más tarde.", "message": "..."}` |

---

## Reglas de Negocio

* Orden de validación: request → existencia de tienda → permisos y rol → tienda activa → existencia de producto → guardado.
* Estado literal: **`PENDIENTE`**, asignado por el servidor (también es el valor por defecto del modelo de dominio).
* `fechaSolicitud = LocalDateTime.now()` se genera en el servidor; `usuarioId` es el ID numérico del solicitante.
* **No se valida** que el producto esté activo, **no se consulta ni descuenta inventario**, **no se genera una orden**, **no hay control de duplicados** y **no se envía notificación**: la visibilidad para el planificador es únicamente el `GET`.
* `ReposicionService` y el adaptador de persistencia no tienen `@Transactional`; la única escritura es el `save` en `solicitudes_reposicion`.
* Si el producto fue eliminado, el listado muestra el fallback `"Producto #<id>"` en lugar de fallar.

---

## Componentes Hexagonales Asociados

* **Controlador REST:** [`ReposicionController.java`](../../../src/main/java/com/example/optiway/infrastructure/adapter/in/rest/ReposicionController.java)
* **Puerto de entrada:** `SolicitarReposicionUseCase.java` · `ObtenerSolicitudesReposicionUseCase.java`
* **Servicio de aplicación:** [`ReposicionService.java`](../../../src/main/java/com/example/optiway/application/service/ReposicionService.java)
* **Autorización:** `AuthorizationService.requirePermisoTienda` / `requireGestionLogistica` + exclusión explícita de ADMINISTRADOR en el servicio
* **DTO request:** [`CrearSolicitudReposicionRequest.java`](../../../src/main/java/com/example/optiway/infrastructure/adapter/in/rest/dto/CrearSolicitudReposicionRequest.java)
* **DTO response:** `SolicitudReposicionResponse.java`
* **Modelo de dominio:** `SolicitudReposicion.java`
* **Puerto de salida:** `SolicitudReposicionRepositoryPort` → `SolicitudReposicionRepositoryAdapter` → `solicitudes_reposicion`

---

## Flujo en Arquitectura Hexagonal

```text
Cliente (Encargado de tienda)
  ↓ POST /api/tiendas/{id}/solicitudes-reposicion + Bearer JWT
SecurityFilterChain → @AuthUserId
  ↓
ReposicionController.crearSolicitudReposicion
  ↓ CrearSolicitudReposicionRequest.toCommand()
SolicitarReposicionUseCase
  ↓
ReposicionService.crearSolicitud
  ├── requirePermisoTienda + esEncargadoTienda → 403
  ├── TiendaRepositoryPort.obtenerPorId → 404 / 409 si inactiva
  ├── ProductoRepositoryPort.findById → 404
  └── SolicitudReposicionRepositoryPort.save → estado PENDIENTE
  ↓
SolicitudReposicionResponse (201)

Planificador: GET /api/solicitudes-reposicion → ReposicionService.obtenerTodasLasSolicitudes (200)
```

---

## Pruebas y Migraciones

* **Tests directos:** [`ReposicionServiceTest.java`](../../../src/test/java/com/example/optiway/ReposicionServiceTest.java) — guardado con estado `PENDIENTE` y datos correctos; rechazo a usuario no encargado; listado del planificador leído desde BD.
* **Sin tests** de validación 400, tienda inactiva (409), producto inexistente (404), exclusión explícita de ADMINISTRADOR ni de endpoints HTTP.
* **Migración SQL:** no existe para `solicitudes_reposicion`; la tabla se crea con `spring.jpa.hibernate.ddl-auto=update`.
