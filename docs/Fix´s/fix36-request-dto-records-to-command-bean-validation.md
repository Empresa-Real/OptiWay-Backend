# Fix 36 — Request DTOs migrados a Java Records con toCommand() y Bean Validation

> **Commit asociado:** este fix (commit `refactor(dto): ... (Fix 36)`) de la serie de estandarización hexagonal.

## 1. Contexto y Diagnóstico del Problema

1. **DTOs mutables tipo JavaBean:** los 10 Request DTOs eran clases con campos privados, constructores vacíos, *getters/setters* y constructores alternativos — código verboso y superficialmente inmutable.
2. **Validación fuera del contrato:** parte de las reglas vivían en `if/throw` del servicio o en el controlador; las anotaciones de `jakarta.validation` no estaban donde Jackson y `@Valid` las esperaban por componente.
3. **Validaciones defensivas redundantes:** presencia de chequeos `request != null` o `campo != null` en métodos ya garantizados por `@Valid`/`@NotNull`.
4. **Conversión ambigua:** convivían `toDomain()` (que instanciaba entidades de dominio en la capa web) y las reglas de negocio dentro de los DTOs.

## 2. Solución Aplicada

- **Migración a `record`** de los 10 Request DTOs, conservando `@Schema` de Swagger y trasladando Bean Validation a los componentes del record:

  `ActualizarRolRequest`, `CambiarEstadoProductoRequest`, `CrearCentroDistribucionRequest`, `CrearProductoRequest`, `CrearTiendaRequest`, `CrearUsuarioRequest`, `EditarProductoRequest`, `RegistrarIngresoMercanciaRequest`, `RegistrarIngresoRequest`, `RegistrarVentaRequest`

- **Único punto de conversión: `toCommand()`**
  ```java
  public CrearProductoCommand toCommand() { ... }
  public EditarProductoCommand toCommand(Long id) { ... }   // id viene del @PathVariable
  ```
  El controlador jamás construye entidades de dominio: `request.toCommand(...)`.
- **Eliminación de `toDomain()` y de chequeos defensivos** redundantes bajo `@Valid`.

## 3. Archivos Intervenidos

Todos en `infrastructure/adapter/in/rest/dto/`:

- `ActualizarRolRequest.java`
- `CambiarEstadoProductoRequest.java`
- `CrearCentroDistribucionRequest.java`
- `CrearProductoRequest.java`
- `CrearTiendaRequest.java`
- `CrearUsuarioRequest.java`
- `EditarProductoRequest.java`
- `RegistrarIngresoMercanciaRequest.java`
- `RegistrarIngresoRequest.java`
- `RegistrarVentaRequest.java`

## 4. Beneficios Obtenidos

1. **Inmutabilidad por lenguaje:** sin setters no hay mutación post-construcción.
2. **Menos ruido:** ~150 líneas de getters/setters/constructores eliminadas; validación declarativa en una sola ubicación.
3. **Aislamiento del dominio:** la capa web ya no sabe cómo se construye un `Producto` o una `Tienda`; solo conoce Commands.

## 5. Observaciones

- **Los records mantienen la compatibilidad de deserialización de Jackson** sin constructor vacío: Jackson usa el canonical constructor y combina Bean Validation vía `@Valid` en el controller.
- `RegistrarIngresoRequest` y `RegistrarIngresoMercanciaRequest` conviven por compatibilidad de endpoints históricos; ambos quedan como record con `toCommand()`.
- El acoplamiento `Controller → toCommand → UseCase` es la **contraparte de `fix35`** (servicio recibe Commands): juntos cierran el flujo de entrada sin fugas de dominio.
- Este fix **no** altera el orden de parámetros de los handlers REST (eso es `fix37`).

## 6. Criterios de Aceptación

- [x] Los 10 Request DTOs son `record` sin setters ni constructores vacíos
- [x] Toda conversión Request → Command pasa por `toCommand()`
- [x] Bean Validation declarativa sobre los componentes del record
- [x] Cero `toDomain()` y cero chequeos defensivos redundantes bajo `@Valid`
- [x] `mvnw clean test` → BUILD SUCCESS, 38/38 pruebas
