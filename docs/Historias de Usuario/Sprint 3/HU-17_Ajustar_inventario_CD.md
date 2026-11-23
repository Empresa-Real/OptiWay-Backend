# HU-17 — Ajustar inventario de mi centro de distribución

**Épica 3:** Operación de ventas e inventario  
**Feature:** Inventario en centro de distribución  
**Sprint:** 3  
**Estado:** Implementado (unificado con HU-13 en `AjustarInventarioUseCase`)

---

## Historia de Usuario

> **Como** Encargado de centro de distribución  
> **Quiero** ajustar la cantidad registrada de un producto  
> **Para** que coincida con el conteo físico de mi centro.

Fuente: `product_backlog.txt:265-273`.

---

## Criterios de Aceptación

1. Al ajustar la cantidad, el sistema actualiza el inventario y registra el ajuste con fecha y usuario responsable.
2. El sistema no permite ingresar una cantidad negativa; muestra error y no guarda el ajuste.

**Implementación:** ambos criterios cubiertos, compartiendo `AjustarInventarioService` con HU-13.

---

## Especificación del Endpoint

* **Método:** `PATCH`
* **Rutas soportadas:** `/api/centros-distribucion/{id}/inventario` y el alias `/api/cd/{id}/inventario`
* **Seguridad:** `Authorization: Bearer <token_jwt_supabase>`; solo el `ENCARGADO_CD` asignado al centro.
* **Path param:** `id` (`Long`) — ID del centro de distribución.
* **Éxito:** `200 OK` (a diferencia de HU-13, que devuelve `201`).
* **Cuerpo de la petición (JSON, obligatorio):**

```json
{
  "productoId": 10,
  "nuevaCantidad": 125
}
```

| Campo | Tipo | Obligatorio | Validación |
| :--- | :--- | :--- | :--- |
| `productoId` | `Long` | **Sí** | `@NotNull("El ID del producto es obligatorio")`, `@Positive("El ID del producto debe ser positivo")` |
| `nuevaCantidad` | `Integer` | **Sí** | `@NotNull("La nueva cantidad es obligatoria")`, `@Min(0, "La nueva cantidad debe ser mayor o igual a cero")` |
| `centroDistribucionId` | `Long` | No | Se ignora: gana siempre el `id` de la URL |
| `tiendaId` | `Long` | No | Se ignora en este endpoint |

> El controlador construye el comando con `tiendaId = null` y el CD tomado de la URL; no es necesario enviar `centroDistribucionId` en el body.

---

## Respuestas Esperadas

| Código HTTP | Condición | Cuerpo de Respuesta |
| :--- | :--- | :--- |
| **`200 OK`** | Ajuste registrado. | `AjusteInventarioResponse`: `id, tiendaId (null), centroDistribucionId, productoId, cantidadAnterior, diferencia, cantidadNueva, usuarioId, fechaAjuste`. |
| **`400 Bad Request`** | Validación Bean Validation / de dominio. | `El ID del producto es obligatorio` · `La nueva cantidad es obligatoria` · `La nueva cantidad debe ser mayor o igual a cero` · `Debe especificar exactamente una ubicación: tiendaId o centroDistribucionId.` · `El ID del centro de distribución debe ser válido.` · `El valor '<valor>' del parámetro 'id' no es válido. Se esperaba un número entero válido.` |
| **`401 Unauthorized`** | Token no suministrado o inválido. | `{"error": "Autenticación requerida. Token no proporcionado o inválido.", "message": "..."}` |
| **`403 Forbidden`** | Usuario que no es el encargado asignado del CD. | `{"error": "Solo el ENCARGADO_CD asignado puede ajustar el inventario de su centro de distribución", "message": "..."}` |
| **`404 Not Found`** | CD, producto o inventario inexistente. | `El centro de distribución con ID X no fue encontrado.` · `El producto con ID X no fue encontrado.` · `El producto no está registrado en el inventario de este centro de distribución.` |
| **`500`** | Error no controlado. | `{"error": "Ocurrió un error interno en el servidor. Por favor intente más tarde.", "message": "..."}` |

> No se devuelve `409` en este flujo. Formato real de error: `{"error":"...","message":"..."}`.

---

## Reglas de Negocio

* Mismo servicio `@Transactional` que HU-13: `diferencia = nuevaCantidad - cantidadAnterior` (puede ser positiva o negativa).
* Auditoría idéntica: `inventarios.cantidad_actual`, `fecha_actualizacion`, `usuario_actualizacion_id` más una fila en `ajustes_inventario` con `usuarioId` y `fechaAjuste`.
* **Bloqueo pesimista:** `findByCentroDistribucionIdAndProductoIdForUpdate` con `@Lock(LockModeType.PESSIMISTIC_WRITE)`.
* **Permisos más restrictivos que HU-13:** `requireEncargadoDelCentro` exige `rol == ENCARGADO_CD` e `id == centro.encargadoId`. ADMINISTRADOR y PLANIFICADOR **no** pueden ajustar inventario de CD (aunque sí pueden consultarlo en HU-16).
* **No se valida** el estado del CD (`"Activo"` / `"Inactivo"`) al ajustar, aunque la regla existe en la entidad (`isActivo()`).
* **No existe campo `motivo`** en el request ni en la respuesta de ajuste.

---

## Componentes Hexagonales Asociados

* **Controlador REST:** [`CentroDistribucionController.java`](../../../src/main/java/com/example/optiway/infrastructure/adapter/in/rest/CentroDistribucionController.java) (`ajustarInventarioCD`)
* **Puerto de entrada:** `AjustarInventarioUseCase.java` · `AjustarInventarioCommand.java`
* **Servicio de aplicación:** `AjustarInventarioService.java`
* **Autorización:** `AuthorizationService.requireEncargadoDelCentro` · `AuthorizedAction.AJUSTAR_INVENTARIO_CENTRO_DISTRIBUCION`
* **DTOs:** [`AjustarInventarioRequest.java`](../../../src/main/java/com/example/optiway/infrastructure/adapter/in/rest/dto/AjustarInventarioRequest.java) · `AjusteInventarioResponse.java`
* **Puertos de salida:** `CentroDistribucionRepositoryPort`, `ProductoRepositoryPort`, `InventarioRepositoryPort`, `AjusteInventarioRepositoryPort`, `AuthorizationPort`
* **Entidades:** `InventarioJpaEntity` (`inventarios`), `AjusteInventarioJpaEntity` (`ajustes_inventario`)

---

## Flujo en Arquitectura Hexagonal

```text
Cliente (Encargado de CD asignado)
  ↓ PATCH /api/centros-distribucion/{id}/inventario  (o /api/cd/{id}/inventario) + Bearer JWT
SecurityFilterChain → @AuthUserId
  ↓
CentroDistribucionController.ajustarInventarioCD
  ↓ new AjustarInventarioCommand(null, idDeLaURL, productoId, nuevaCantidad)
AjustarInventarioUseCase
  ↓
AjustarInventarioService  [@Transactional]
  ├── requireEncargadoDelCentro → 403 para ADMIN/PLANIFICADOR u otro encargado
  ├── CentroDistribucionRepositoryPort.obtenerPorId
  ├── ProductoRepositoryPort.findById
  ├── InventarioRepositoryPort.findByCentroDistribucionIdAndProductoIdForUpdate  [@Lock PESSIMISTIC_WRITE]
  ├── inventarioRepository.save
  └── AjusteInventarioRepositoryPort.guardar → ajustes_inventario
  ↓
AjusteInventarioResponse (200)
```

---

## Pruebas y Migraciones

* **Tests directos:** [`AjustarInventarioServiceTest.java`](../../../src/test/java/com/example/optiway/AjustarInventarioServiceTest.java) — ajuste de CD con `diferencia = 25`, `usuarioId = 200L`, `tiendaId = null` y auditoría guardada.
* **Sin tests** de `requireEncargadoDelCentro`, de endpoints HTTP ni de `@Lock`.
* **Migración SQL:** no existe; las tablas `inventarios` y `ajustes_inventario` se crean con `spring.jpa.hibernate.ddl-auto=update`.
