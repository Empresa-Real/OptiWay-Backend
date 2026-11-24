# HU-20 — Crear recomendación de reposición

**Épica 4:** Reposición y seguimiento de órdenes  
**Feature:** Recomendaciones de reposición  
**Sprint:** 3  
**Estado:** Implementado (integrado desde `origin/feature/HU-20`)

---

## Historia de Usuario

> **Como** Analista/Planificador de cadena de suministro  
> **Quiero** crear una recomendación de reposición indicando el producto, la cantidad, el centro de distribución de origen y la tienda de destino, a partir de lo que veo en el inventario consolidado  
> **Para** decidir con criterio qué trasladar y hacia dónde.

Fuente: `product_backlog.txt:313-323`.

---

## Criterios de Aceptación

1. Al crear la recomendación con producto, cantidad, origen y destino, el sistema la registra en estado "Propuesta".
2. El sistema no permite seleccionar un CD de origen sin disponibilidad suficiente; muestra error y no crea la recomendación.
3. Si el CD elegido no está en la zona de cobertura de la tienda destino, el sistema advierte la inconsistencia y permite confirmar o cambiar el origen.

**Implementación:** los tres criterios están cubiertos. El backlog escribe `"Propuesta"`; la implementación usa el literal **`PROPUESTA`** (mayúsculas).

---

## Especificación de Endpoints

### 1. Crear recomendación

* **Método:** `POST`
* **Ruta:** `/api/recomendaciones-reposicion`
* **Seguridad:** `Authorization: Bearer <token_jwt_supabase>`; **solo rol `PLANIFICADOR`** (ADMINISTRADOR excluido).
* **Cuerpo de la petición (JSON, obligatorio):**

```json
{
  "productoId": 10,
  "cantidad": 25,
  "centroDistribucionOrigenId": 20,
  "tiendaDestinoId": 30,
  "confirmarFueraDeCobertura": false
}
```

| Campo | Tipo | Validación |
| :--- | :--- | :--- |
| `productoId` | `Long` | `@NotNull("El ID del producto es obligatorio.")`, `@Positive("El ID del producto debe ser positivo.")` |
| `cantidad` | `Integer` | `@NotNull("La cantidad es obligatoria.")`, `@Positive("La cantidad debe ser mayor que cero.")` |
| `centroDistribucionOrigenId` | `Long` | `@NotNull("El ID del Centro de Distribución de origen es obligatorio.")`, `@Positive("El ID del Centro de Distribución debe ser positivo.")` |
| `tiendaDestinoId` | `Long` | `@NotNull("El ID de la tienda de destino es obligatorio.")`, `@Positive("El ID de la tienda debe ser positivo.")` |
| `confirmarFueraDeCobertura` | `Boolean` | Opcional; debe ser `true` para confirmar un origen fuera de cobertura |

* Éxito: `201 Created` con `RecomendacionReposicionResponse`.

### 2. Consultar recomendaciones

* **Método:** `GET`
* **Ruta:** `/api/recomendaciones-reposicion`
* **Éxito:** `200 OK` con `List<RecomendacionReposicionResponse>`; acción `LISTAR_RECOMENDACIONES_REPOSICION`.

---

## Respuestas Esperadas

| Código HTTP | Condición | Cuerpo de Respuesta |
| :--- | :--- | :--- |
| **`201 Created`** | Propuesta creada. | `RecomendacionReposicionResponse`: `id, productoId, nombreProducto, cantidad, centroDistribucionOrigenId, tiendaDestinoId, estado ("PROPUESTA"), fueraDeCobertura, creadoPor, fechaCreacion, resueltoPorUsuarioId, accionTomada, fechaResolucion, motivoRechazo, cantidadPropuesta, centroDistribucionPropuestoId`. |
| **`400 Bad Request`** | Validación Bean Validation / servicio. | `El ID del producto es obligatorio.` · `La cantidad debe ser mayor que cero.` · `Los identificadores de producto, Centro de Distribución y tienda son obligatorios y deben ser válidos.` · `La cantidad solicitada debe ser un número mayor que cero.` · `Debe enviar los datos de la recomendación.` |
| **`400 Bad Request`** | Producto/tienda/CD inactivo. | `No se puede operar con un producto inactivo.` · `No se puede operar con una tienda inactiva.` · `No se puede operar con un Centro de Distribución inactivo.` |
| **`400 Bad Request`** | Stock insuficiente. | `El Centro de Distribución seleccionado no tiene inventario registrado para este producto.` · `Stock insuficiente en el Centro de Distribución. Disponible: X. Cantidad solicitada: Y.` |
| **`401 Unauthorized`** | Token no suministrado o inválido. | `{"error": "Autenticación requerida. Token no proporcionado o inválido.", "message": "..."}` |
| **`403 Forbidden`** | Rol distinto de PLANIFICADOR. | `{"error": "Solo un PLANIFICADOR puede crear recomendaciones de reposición", "message": "..."}` |
| **`404 Not Found`** | Entidad inexistente. | `No se encontró el producto indicado.` · `No se encontró la tienda de destino.` · `No se encontró el Centro de Distribución de origen.` · `No se encontró el usuario que crea la recomendación.` |
| **`409 Conflict`** | CD fuera de zona de cobertura sin confirmar. | `{"error": "La tienda no pertenece a la zona de cobertura del Centro de Distribución seleccionado. Confirme la operación enviando confirmarFueraDeCobertura: true o seleccione otro Centro de Distribución.", "message": "..."}` |
| **`500`** | Error no controlado. | `{"error": "Ocurrió un error interno en el servidor. Por favor intente más tarde.", "message": "..."}` |

> Formato real de error: `{"error":"...","message":"..."}`.

---

## Reglas de Negocio

* El servicio es `@Transactional` y valida: IDs `> 0`, cantidad `> 0`, producto/tienda/CD activos, existencia de inventario en el CD y `stockDisponible >= cantidad`.
* **La cantidad no se calcula automáticamente**: se copia literalmente del body. No hay lógica de promedios, demanda estacional ni sugerencias en el backend (HU-19 no implementada).
* **No se descuenta stock**: una propuesta no ejecuta el traslado; el comentario del servicio es explícito: `// Una propuesta no ejecuta el traslado ni descuenta existencias.`
* **Cobertura:** `fueraDeCobertura = true` si el CD no está en `tiendasAbastecidasIds` de la tienda. Sin `confirmarFueraDeCobertura: true` → `409`; con confirmación se guarda igual y se marca la bandera.
* Estado inicial: `PROPUESTA`. Trazabilidad inicial: `creadoPor = authUserId` del JWT, `fechaCreacion = LocalDateTime.now()`. Los campos de resolución (`resueltoPor`, `accionTomada`, `fechaResolución`, `motivoRechazo`, `cantidadPropuesta`, `centroDistribucionPropuestoId`) quedan `null`.
* No hay validación de recomendación duplicada ni bloqueo pesimista en la creación (el bloqueo se introduce en HU-21 al resolver).
* `RecomendacionReposicionService` y su adaptador de persistencia están `@Transactional`; `consultar` es `readOnly`.

---

## Componentes Hexagonales Asociados

* **Controlador REST:** [`RecomendacionReposicionController.java`](../../../src/main/java/com/example/optiway/infrastructure/adapter/in/rest/RecomendacionReposicionController.java) (`crear`, `consultar`)
* **Puerto de entrada:** `CrearRecomendacionReposicionUseCase.java` · `CrearRecomendacionReposicionCommand.java`
* **Servicio de aplicación:** [`RecomendacionReposicionService.java`](../../../src/main/java/com/example/optiway/application/service/RecomendacionReposicionService.java) (`crear`, `consultar`)
* **Autorización:** `AuthorizationService.requirePlanificador` · `AuthorizedAction.CREAR_RECOMENDACION_REPOSICION` / `LISTAR_RECOMENDACIONES_REPOSICION`
* **DTO request:** [`CrearRecomendacionReposicionRequest.java`](../../../src/main/java/com/example/optiway/infrastructure/adapter/in/rest/dto/CrearRecomendacionReposicionRequest.java)
* **DTO response:** `RecomendacionReposicionResponse.java`
* **Modelo / entidad:** `RecomendacionReposicion.java` · `RecomendacionReposicionJpaEntity.java`
* **Puertos de salida:** `ProductoRepositoryPort`, `TiendaRepositoryPort`, `CentroDistribucionRepositoryPort`, `InventarioRepositoryPort`, `RecomendacionReposicionRepositoryPort`
* **Adaptador:** `RecomendacionReposicionRepositoryAdapter` → `recomendaciones_reposicion`

---

## Flujo en Arquitectura Hexagonal

```text
Cliente (Planificador)
  ↓ POST /api/recomendaciones-reposicion + Bearer JWT
SecurityFilterChain → @AuthUserId (UUID del claim 'sub')
  ↓
RecomendacionReposicionController.crear → CrearRecomendacionReposicionRequest.toCommand()
  ↓
CrearRecomendacionReposicionUseCase → RecomendacionReposicionService.crear [@Transactional]
  ├── requirePlanificador → 403 para ADMIN u otros roles
  ├── validarProductoActivo / validarTiendaActiva / validarCentroYDisponibilidad
  ├── esFueraDeCobertura → 409 si no se confirmó
  └── RecomendacionReposicionRepositoryPort.guardar
        → UsuarioJpaRepository.findByAuthUserId (creado_por)
        → recomendaciones_reposicion (estado PROPUESTA)
  ↓
RecomendacionReposicionResponse (201)
```

---

## Pruebas y Migraciones

* **Tests directos:** [`RecomendacionReposicionServiceTest.java`](../../../src/test/java/com/example/optiway/RecomendacionReposicionServiceTest.java) — propuesta válida sin descuento de stock; stock insuficiente sin guardar; fuera de cobertura sin confirmar (`409`); fuera de cobertura con confirmación.
* **Sin tests HTTP/MockMvc** para validar los mensajes `@NotNull`/`@Positive` por la vía `@Valid`.
* **Migración:** no hay una migración específica de HU-20. La tabla y sus columnas se derivan del mapeo JPA con `ddl-auto=update`; la migración compartida [`hu21_ordenes_y_auditoria_recomendaciones.sql`](../../migrations/hu21_ordenes_y_auditoria_recomendaciones.sql) añade las columnas de trazabilidad usadas al resolver en HU-21.
