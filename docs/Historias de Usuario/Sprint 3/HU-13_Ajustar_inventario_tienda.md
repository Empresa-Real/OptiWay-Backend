# HU-13 — Ajustar inventario de mi tienda

**Épica 3:** Operación de ventas e inventario  
**Feature:** Ventas e inventario en tienda  
**Sprint:** 3  
**Estado:** Implementado (unificado con HU-17 en `AjustarInventarioUseCase`)

---

## Historia de Usuario

> **Como** Encargado de tienda  
> **Quiero** ajustar la cantidad registrada de un producto  
> **Para** que coincida con el conteo físico y evitar diferencias entre lo real y lo registrado en el sistema.

Fuente: `product_backlog.txt:243-251`.

---

## Criterios de Aceptación

1. Al ajustar la cantidad de un producto, el sistema actualiza el inventario y registra el ajuste con fecha y usuario responsable.
2. El sistema no permite ingresar una cantidad negativa; muestra error y no guarda el ajuste.

**Implementación:** ambos criterios están cubiertos. La auditoría se persiste en `ajustes_inventario` y el stock se bloquea con `PESSIMISTIC_WRITE` durante la operación.

---

## Especificación del Endpoint

* **Método:** `POST`
* **Ruta:** `/api/inventario/ajustes` (no existe un endpoint bajo `/api/tiendas/...`)
* **Seguridad:** `Authorization: Bearer <token_jwt_supabase>`; identidad del JWT en `@AuthUserId`.
* **Respuesta correcta:** `201 Created` (`@ResponseStatus(HttpStatus.CREATED)`).
* **Parámetros:** ninguno de ruta ni query; la tienda viaja en el body.
* **Cuerpo de la petición (JSON, obligatorio):**

```json
{
  "tiendaId": 20,
  "productoId": 10,
  "nuevaCantidad": 32
}
```

| Campo | Tipo | Obligatorio | Validación |
| :--- | :--- | :--- | :--- |
| `tiendaId` | `Long` | Sí (para este endpoint) | `@Positive("El ID de la tienda debe ser positivo")` |
| `centroDistribucionId` | `Long` | No | `@Positive("El ID del centro de distribución debe ser positivo")` |
| `productoId` | `Long` | **Sí** | `@NotNull("El ID del producto es obligatorio")`, `@Positive("El ID del producto debe ser positivo")` |
| `nuevaCantidad` | `Integer` | **Sí** | `@NotNull("La nueva cantidad es obligatoria")`, `@Min(0, "La nueva cantidad debe ser mayor o igual a cero")` |

> Debe indicarse **exactamente una** ubicación (`tiendaId` o `centroDistribucionId`). En este endpoint se usa `tiendaId`; si se envía también un CD, el servicio rechaza la petición.

---

## Respuestas Esperadas

| Código HTTP | Condición | Cuerpo de Respuesta |
| :--- | :--- | :--- |
| **`201 Created`** | Ajuste registrado. | `AjusteInventarioResponse`: `id, tiendaId, centroDistribucionId, productoId, cantidadAnterior, diferencia, cantidadNueva, usuarioId, fechaAjuste`. |
| **`400 Bad Request`** | Validación Bean Validation / de dominio. | Mensajes literales: `El ID del producto es obligatorio`, `La nueva cantidad es obligatoria`, `La nueva cantidad debe ser mayor o igual a cero`, `Debe especificar exactamente una ubicación: tiendaId o centroDistribucionId.`, `Los datos del ajuste son obligatorios.`, `El ID de la tienda debe ser válido.`, `El ID del producto debe ser válido.` |
| **`401 Unauthorized`** | Token no suministrado o inválido. | `{"error": "Autenticación requerida. Token no proporcionado o inválido.", "message": "..."}` |
| **`403 Forbidden`** | Usuario sin permiso sobre la tienda. | `{"error": "No tiene permisos para ajustar el inventario de esta tienda", "message": "..."}` |
| **`404 Not Found`** | Tienda, producto o inventario inexistente. | `La tienda con ID X no fue encontrada.` · `El producto con ID X no fue encontrado.` · `El producto no está registrado en el inventario de esta tienda.` |
| **`500`** | Error no controlado. | `{"error": "Ocurrió un error interno en el servidor. Por favor intente más tarde.", "message": "..."}` |

> No se devuelve `409` en este flujo. Formato real de error: `{"error":"...","message":"..."}`.

---

## Reglas de Negocio

* `AjustarInventarioService.ajustarInventario` está marcado con `@Transactional`.
* **Cantidad final:** se guarda tal cual se recibe; `diferencia = nuevaCantidad - cantidadAnterior` (puede ser negativa, p. ej. 40 → 32 da `-8`).
* **Sin negativos:** validación `@Min(0)` en el DTO y comprobación `nuevaCantidad < 0` en el servicio; si falla, no se guarda nada (`verifyNoInteractions` en los tests).
* **Auditoría:** se actualizan `inventarios.cantidad_actual`, `fecha_actualizacion` y `usuario_actualizacion_id`, y se inserta un `AjusteInventario` con `cantidadAnterior`, `diferencia`, `cantidadNueva`, `usuarioId` (ID numérico de BD, no UUID) y `fechaAjuste`.
* **Concurrencia:** `findByTiendaIdAndProductoIdForUpdate` con `@Lock(LockModeType.PESSIMISTIC_WRITE)`.
* **No se valida** que la tienda esté activa, ni la existencia de un campo `motivo` (no existe en el contrato).
* **No hay límite superior** de stock ni validación frente al `stockMinimo`.
* `Inventario.validarUbicacionExclusiva()` impide que un registro tenga simultáneamente tienda y CD.

---

## Componentes Hexagonales Asociados

* **Controlador REST:** [`InventarioController.java`](../../../src/main/java/com/example/optiway/infrastructure/adapter/in/rest/InventarioController.java) (`ajustarInventario`)
* **Puerto de entrada:** [`AjustarInventarioUseCase.java`](../../../src/main/java/com/example/optiway/application/port/in/AjustarInventarioUseCase.java) · [`AjustarInventarioCommand.java`](../../../src/main/java/com/example/optiway/application/port/in/AjustarInventarioCommand.java)
* **Servicio de aplicación:** [`AjustarInventarioService.java`](../../../src/main/java/com/example/optiway/application/service/AjustarInventarioService.java) (`@Transactional`)
* **Autorización:** `AuthorizationService.requirePermisoTienda` · `AuthorizedAction.AJUSTAR_INVENTARIO_TIENDA`
* **DTOs:** [`AjustarInventarioRequest.java`](../../../src/main/java/com/example/optiway/infrastructure/adapter/in/rest/dto/AjustarInventarioRequest.java) · [`AjusteInventarioResponse.java`](../../../src/main/java/com/example/optiway/application/port/in/dto/AjusteInventarioResponse.java)
* **Modelos de dominio:** `Inventario`, `AjusteInventario`
* **Puertos de salida:** `InventarioRepositoryPort` (con variantes `...ForUpdate`), `AjusteInventarioRepositoryPort`, `TiendaRepositoryPort`, `ProductoRepositoryPort`, `AuthorizationPort`
* **Entidades:** `InventarioJpaEntity` (`inventarios`), `AjusteInventarioJpaEntity` (`ajustes_inventario`)

---

## Flujo en Arquitectura Hexagonal

```text
Cliente (Encargado de tienda / ADMIN / PLANIFICADOR)
  ↓ POST /api/inventario/ajustes + Bearer JWT
SecurityFilterChain → @AuthUserId
  ↓
InventarioController → AjustarInventarioRequest.toCommand()
  ↓
AjustarInventarioUseCase
  ↓
AjustarInventarioService  [@Transactional]
  ├── requirePermisoTienda → 403 si no aplica
  ├── TiendaRepositoryPort.obtenerPorId
  ├── ProductoRepositoryPort.findById
  ├── InventarioRepositoryPort.findByTiendaIdAndProductoIdForUpdate  [@Lock PESSIMISTIC_WRITE]
  ├── inventarioRepository.save (cantidad + fecha + usuario)
  └── AjusteInventarioRepositoryPort.guardar → ajustes_inventario
  ↓
AjusteInventarioResponse (201)
```

---

## Pruebas y Migraciones

* **Tests directos:** [`AjustarInventarioServiceTest.java`](../../../src/test/java/com/example/optiway/AjustarInventarioServiceTest.java) — ajuste de tienda con `diferencia = -8` y auditoría; cantidad negativa rechazada sin escrituras; ubicación ambigua/nula rechazada.
* **Cobertura indirecta:** `AuthorizationServiceTest` (`requirePermisoTienda` admite ADMIN, PLANIFICADOR y encargado asignado).
* **Sin tests HTTP/MockMvc** ni tests de `@Lock` o de la tabla `ajustes_inventario`.
* **Migración SQL:** no existe; `inventarios` y `ajustes_inventario` se crean con `spring.jpa.hibernate.ddl-auto=update`.
