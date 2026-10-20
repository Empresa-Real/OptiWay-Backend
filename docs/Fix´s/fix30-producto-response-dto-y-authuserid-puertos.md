# Fix 30 — ProductoResponse DTO y estandarización de authUserId en los puertos de producto

> **Commit asociado:** `738fb90` — `refactor(producto): implementar ProductoResponse DTO, estandarizar authUserId en puertos de producto y mover ProductoStockResponse a port.in.dto` (2026-10-19 01:48)
> **Nota:** doc retroactivo generado dentro del plan de estandarización hexagonal (serie fix28–fix37).

## 1. Contexto y Diagnóstico del Problema

1. **Fuga de Dominio hacia la Capa REST:** los casos de uso de producto devolvían la entidad `Producto` del dominio, por lo que `ProductoController` trabajaba directamente con `com.example.optiway.domain.model.Producto` y exponía el modelo interno (incluido `stockActual` y estado JPA) en el contrato HTTP.
2. **DTO mal ubicado:** `ProductoStockResponse` vivía en `infrastructure.adapter.in.rest.dto` pese a ser un contrato de salida de la aplicación, no de la capa web.
3. **Firmas sin uniformidad:** los puertos de producto no seguían la convención de `authUserId` como primer parámetro.

## 2. Solución Aplicada

- **Nuevo DTO `ProductoResponse`** en `application.port.in.dto` con fábrica estática `from(Producto)` que proyecta únicamente datos de presentación (identificador, nombre, categoría, precio, unidad de medida, stock, estado).
- **`ProductoStockResponse` migrado** de `rest/dto` a `port/in/dto`.
- **Puertos de mutación de producto** (`CrearProductoUseCase`, `EditarProductoUseCase`): `authUserId` como primer parámetro y retorno `ProductoResponse` en lugar de `Producto`.
- **`ProductoService`** realiza el mapeo dominio → DTO; **`ProductoController`** delega sin importar el dominio en sus retornos.

## 3. Archivos Intervenidos

| Estado | Archivo |
|---|---|
| Nuevo | `application/port/in/dto/ProductoResponse.java` |
| Modificado | `application/port/in/CrearProductoUseCase.java` |
| Modificado | `application/port/in/EditarProductoUseCase.java` |
| Modificado | `application/port/in/dto/ProductoStockResponse.java` |
| Modificado | `application/service/ProductoService.java` |
| Modificado | `infrastructure/adapter/in/rest/ProductoController.java` |
| Eliminado | `infrastructure/adapter/in/rest/dto/ProductoStockResponse.java` |

## 4. Beneficios Obtenidos

1. **Contrato HTTP estable:** los consumidores REST ven un DTO de presentación, no el modelo interno; refactors de dominio dejan de romper la API.
2. **Arquitectura Hexagonal:** el DTO de salida vive en la puerta de entrada de aplicación (`port.in.dto`), no en el adaptador.
3. **Consistencia de firmas:** `authUserId` como primer argumento, igual que el resto de la serie.

## 5. Observaciones

- Fue el **primer fix de la serie de Response DTOs**; el mismo patrón se replicó después en Tienda (`fix31`), en el resto de agregados (`fix32`) y en la capa de servicios (`fix35`).
- En este commit el mapeo `from(...)` aún podía vivir cerca del controlador; la regla final *"el controlador solo delega, el servicio mapea"* se documenta en `fix35`/`fix37`.
- `graph.html` se modifica en cada build por la fase `copy-graph-html` de Maven (sidecar del knowledge graph).
- La construcción de producto mediante `CrearProductoCommand`/`EditarProductoCommand` llega en `fix33` y `fix28`.

## 6. Criterios de Aceptación

- [x] `ProductoController` retorna `ProductoResponse`, nunca `domain.model.Producto`
- [x] `ProductoStockResponse` vive en `application/port/in/dto`
- [x] `authUserId` es el primer parámetro de los puertos de producto
- [x] Suite verificada en estado integrado: `mvnw clean test` → BUILD SUCCESS, 38/38
