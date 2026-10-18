# Informe de Correcciones y Estandarización: HU-06 — Desactivar y Reactivar Productos (Fix 9)

Este documento detalla la resolución de conflictos e integración de la **HU-06 (Desactivar y Reactivar Productos)** en la rama `main`, unificando el modelo de datos hacia booleanos seguros, desacoplando la máquina de confirmación hacia el Frontend y preservando la integridad de HU-04 y HU-05.

---

## 1. Resumen Ejecutivo y Alcance

La **HU-06** permite al `ADMINISTRADOR` activar o desactivar productos del catálogo maestro:
* `PATCH /api/productos/{id}/estado` con cuerpo `{"activo": true/false}`.
* Un producto desactivado conserva su registro e historial en el sistema pero no puede ser utilizado en nuevas operaciones comerciales.
* Se provee el endpoint auxiliar `GET /api/productos/{id}/tiene-stock` para que el **Frontend** pueda consultar de forma previa si el producto posee existencias activas en inventario y presentar el diálogo de confirmación interactivo al usuario.

---

## 2. Conflictos y Brechas Detectadas Previas al Fix

| Componente | Brecha / Conflicto Previo | Impacto |
| :--- | :--- | :--- |
| **Modelo de Datos** | HU-04/05 usaba `String estado` ("activo"/"inactivo") mientras HU-06 introdujo `Boolean activo`. | Inconsistencia de tipos en base de datos PostgreSQL y entidades JPA. |
| **Lógica de Confirmación** | HU-06 original intentó implementar la confirmación interactiva en el backend con una clase intermedia `CambioEstadoProductoResultado`, flags `confirmar` y mensajes de texto en el payload. | Violación de responsabilidades: la confirmación con el usuario es una interacción de UI que pertenece al **Frontend**. Hacía el backend complejo y frágil. |
| **Pérdida de Código** | La rama `HU-06` sobreescribía `ProductoService` y `ProductoController` borrando los métodos de creación, edición y consulta de productos de HU-04 y HU-05. | Regresión catastrófica de HU-04 y HU-05 si se mergeaba ciegamente. |
| **Seguridad** | Uso de `@AuthenticationPrincipal Jwt jwt` y parseo manual. | Inconsistencia con el estándar `@AuthUserId` establecido en Fix 7. |

---

## 3. Soluciones Implementadas

### A. Unificación del Modelo hacia Booleanos (`Boolean activo`)
Se estandarizó el uso de booleanos en el dominio y persistencia:
* **[Producto.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/domain/model/Producto.java):** Campo `private Boolean activo = true;` con métodos `isActivo()`, `setActivo(Boolean)` y getters/setters sincronizados de `estado` para retrocompatibilidad total.
* **[ProductoJpaEntity.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/out/persistence/ProductoJpaEntity.java):** Columna `@Column(nullable = false, columnDefinition = "boolean default true") private Boolean activo = true;`.
* **[ProductoRepositoryAdapter.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/out/persistence/ProductoRepositoryAdapter.java):** Mapeo de `activo` en operaciones `save` y `update`.

### B. Desacoplamiento de la Confirmación hacia el Frontend
1. **Endpoint de Ejecución Directa:**
   `PATCH /api/productos/{id}/estado` recibe directamente [CambiarEstadoProductoRequest.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/dto/CambiarEstadoProductoRequest.java) (`{"activo": false}`).
   * Valida que quien ejecuta sea `ADMINISTRADOR`.
   * Actualiza el estado y persiste inmediatamente en base de datos.
2. **Endpoint Auxiliar de Consulta de Existencias:**
   `GET /api/productos/{id}/tiene-stock` retorna `{"tieneStock": true/false}`.
   * Permite al **Frontend** consultar si el producto tiene unidades en tiendas o CDs antes de abrir la ventana modal de confirmación:
     > *"Atención: El producto cuenta con existencias en inventario. ¿Desea desactivarlo de todas formas?"*

### C. Cohesión de Casos de Uso en la Capa de Servicio
En [ProductoService.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/service/ProductoService.java):
* Se implementaron conjuntamente `CrearProductoUseCase`, `ObtenerProductosUseCase`, `EditarProductoUseCase` y `CambiarEstadoProductoUseCase` sin pérdidas de funcionalidad.
* Se inyectó `InventarioRepositoryPort` para verificar existencias mediante `existeStockActivoDeProducto(productoId)`.

### D. Controlador REST con `@AuthUserId`
En [ProductoController.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/ProductoController.java):
* Se integraron los 4 endpoints de producto de forma limpia y desacoplada, utilizando `@AuthUserId UUID authUserId`.

---

## 4. Matriz Comparativa

| Aspecto | Implementación HU-06 Original | Implementación con Fix 9 |
| :--- | :--- | :--- |
| **Tipo de Estado** | Conflicto entre `String` y `Boolean` | `Boolean activo` unificado |
| **Confirmación de Stock** | Máquina de estados en el Backend (`confirmar: true/false`) | Frontend consulta `/tiene-stock` y confirma en UI |
| **Payload PATCH** | Objeto complejo con textos de advertencia | `{"activo": false}` directo |
| **Integración con HU-04/05** | Destructiva (borraba métodos previos) | Armónica y aditiva (coexisten todos los casos de uso) |
| **Inyección de Identidad** | `UUID.fromString(jwt.getSubject())` | `@AuthUserId UUID authUserId` |

---

## 5. Estado de Compilación

* **Herramienta:** Maven Wrapper (`mvnw compile -DskipTests`)
* **Resultado:** `BUILD SUCCESS`
