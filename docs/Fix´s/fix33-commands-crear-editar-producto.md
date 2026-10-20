# Fix 33 — Commands inmutables CrearProductoCommand y EditarProductoCommand

> **Commit asociado:** `cece2d4` — `refactor(producto): implementar CrearProductoCommand y EditarProductoCommand desacoplando UseCases del dominio y reforzando validaciones` (2026-10-19 15:08)
> **Nota:** doc retroactivo generado dentro del plan de estandarización hexagonal (serie fix28–fix37).

## 1. Contexto y Diagnóstico del Problema

1. **Firma posicional larga en creación/edición de producto:** `CrearProductoUseCase` y `EditarProductoUseCase` recibían 4–5 argumentos escalares dispersos (`identificador`, `nombre`, `categoria`, `precio`, `unidadMedida`, `id`), obligando a los adaptadores a conocer el orden exacto de los parámetros.
2. **Validación frágil y repartida:** las reglas (nombre obligatorio, precio no negativo) se validaban con `if`/`throw` dentro del servicio o no se validaban del todo, en lugar de declararse en el contrato.
3. **Contrato de entrada sin inmutabilidad:** los Request DTOs de la capa web eran clases con getters/setters mutables y constructores vacíos para Jackson.

## 2. Solución Aplicada

- **Nuevos records en `application.port.in`:**
  - `CrearProductoCommand(identificador, nombre, categoria, precio, unidadMedida)` con `@NotBlank` y `@DecimalMin("0.0")`.
  - `EditarProductoCommand(id, nombre, categoria, precio, unidadMedida)` con `@NotNull` en `id`.
- **Firmas estandarizadas:**
  ```java
  ProductoResponse crearProducto(UUID authUserId, CrearProductoCommand command);
  ProductoResponse editarProducto(UUID authUserId, EditarProductoCommand command);
  ```
- **`ProductoService`:** validaciones de negocio reforzadas con helper `requerido(valor, mensaje)` (trim + no-blanco) además del Bean Validation del record.
- **Request DTOs de producto** ganaron `toCommand()` como única vía de conversión (controlador: `request.toCommand(id)`), eliminando la construcción de entidades de dominio en la capa web.

## 3. Archivos Intervenidos

| Estado | Archivo |
|---|---|
| Nuevo | `application/port/in/CrearProductoCommand.java` |
| Nuevo | `application/port/in/EditarProductoCommand.java` |
| Modificado | `application/port/in/CrearProductoUseCase.java`, `EditarProductoUseCase.java` |
| Modificado | `application/service/ProductoService.java` |
| Modificado | `infrastructure/adapter/in/rest/ProductoController.java` |
| Modificado | `rest/dto/CrearProductoRequest.java`, `rest/dto/EditarProductoRequest.java` |

## 4. Beneficios Obtenidos

1. **Contrato autocontenido:** Bean Validation vive en el Command, ejecutable desde cualquier adaptador (REST, CLI, batch).
2. **Inmutabilidad:** los records no exponen setters; el command no puede mutar durante el caso de uso.
3. **Firmas estables:** añadir un campo al producto es agregarlo al record, no reordenar 5 parámetros posicionales.

## 5. Observaciones

- Fue el **primer Command de producto**; la serie se completó en `fix28` con `CrearUsuarioCommand`, `CrearTiendaCommand`, `CrearCentroDistribucionCommand`, `ActualizarRolCommand` y `CambiarEstadoProductoCommand`.
- Este fix **creó los Commands**; el orden `authUserId`-primero en controladores y la migración de los Request DTOs a `record` completo llegan en `fix36` y `fix37`.
- `graph.html` es un sidecar generado por Maven (`copy-graph-html`).

## 6. Criterios de Aceptación

- [x] Los UseCases de producto no reciben argumentos escalares dispersos ni entidades de dominio
- [x] Bean Validation declarativa en ambos Commands (`@NotBlank`, `@NotNull`, `@DecimalMin`)
- [x] Los Request DTOs construyen el Command vía `toCommand()`
- [x] Negocio reforzado con `requerido(...)` en el servicio
