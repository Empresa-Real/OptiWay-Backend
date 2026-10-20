# Fix 31 — TiendaResponse: eliminación de la fuga de dominio en el endpoint de tiendas

> **Commit asociado:** `7bf0f05` — `refactor(tienda): encapsular respuestas en TiendaResponse eliminando fuga de dominio` (2026-10-19 01:55)
> **Nota:** doc retroactivo generado dentro del plan de estandarización hexagonal (serie fix28–fix37).

## 1. Contexto y Diagnóstico del Problema

1. **`POST /api/tiendas` devolvía la entidad `Tienda`** directamente: el contrato HTTP exponía el modelo de dominio completo (con su colección de centros de distribución), acoplando la API a la estructura interna y facilitando fugas de campos no previstos.
2. **Sin documentación OpenAPI:** los endpoints de tiendas carecían de `@Tag`/`@Operation`, degradando el Swagger UI.
3. **Validación defensiva redundante:** el controlador hacía `request != null ? request.toDomain() : null`, comprobación inútil cuando `@Valid` ya garantiza el body.

## 2. Solución Aplicada

- **Nuevo DTO `TiendaResponse`** en `application.port.in.dto` con fábrica `from(Tienda)` que expone id, nombre, dirección, ciudad, estado y encargado, sin colecciones completas del agregado.
- **`TiendaController`** pasa a retornar `TiendaResponse` en `crearTienda`, `obtenerTiendas` y `asignarEncargado`.
- **Anotaciones Swagger** añadidas (`@Tag`, `@Operation` con requerimientos de rol).
- **Eliminada la comprobación defensiva** `request != null` (redundante con `@Valid`).

## 3. Archivos Intervenidos

| Estado | Archivo |
|---|---|
| Nuevo | `application/port/in/dto/TiendaResponse.java` |
| Modificado | `infrastructure/adapter/in/rest/TiendaController.java` |

## 4. Beneficios Obtenidos

1. **Contrato desacoplado:** la API de tiendas ya no cambia si cambia el modelo `Tienda`.
2. **Seguridad de datos:** no se filtran relaciones internas del agregado en las respuestas.
3. **Swagger autoexplicativo:** cada endpoint declara resumen, descripción y rol requerido.

## 5. Observaciones

- Este commit **solo tocó la capa REST + el DTO**: el mapeo `from(...)` se hacía todavía en el controlador y `TiendaController` seguía importando `domain.model.Tienda` para ese mapeo. La eliminación total de imports de dominio en controladores y la migración del mapeo a los servicios se documentan en `fix35` y `fix37`.
- Los retornos de `CrearTiendaUseCase`/`AsignarEncargadoTiendaUseCase` pasaron de entidades de dominio a `TiendaResponse` en la tanda posterior de puertos (incluida en `fix28` y `fix35`).
- `graph.html` es un sidecar generado por Maven (`copy-graph-html`).

## 6. Criterios de Aceptación

- [x] `POST/GET/PATCH` de tiendas retornan `TiendaResponse`
- [x] El endpoint ya no expone la colección `centrosDistribucion` completa
- [x] Todos los endpoints de tiendas documentados con `@Operation`
- [x] Sin validaciones defensivas redundantes bajo `@Valid`
