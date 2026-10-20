# Fix 32 — DTOs de respuesta para CD, Usuarios, Ingresos y Ventas + centralización de errores en español

> **Commit asociado:** `70343a2` — `refactor(dto): encapsular respuestas en DTOs de aplicacion, estandarizar errores en espanol y eliminar fugas de dominio` (2026-10-19 03:27)
> **Nota:** doc retroactivo generado dentro del plan de estandarización hexagonal (serie fix28–fix37).

## 1. Contexto y Diagnóstico del Problema

1. **Fugas de dominio restantes en 4 agregados:** los endpoints de Centros de Distribución, Usuarios, Ingresos de Mercancía y Ventas retornaban entidades de dominio (`CentroDistribucion`, `Usuario`, `IngresoMercancia`, `Venta`).
2. **DTO duplicado:** existía `IngresoMercanciaResponse` tanto en `rest/dto` como necesidad de uno en la capa de aplicación — dos clases con el mismo nombre en paquetes distintos.
3. **Errores inconsistentes:** `ApiExceptionHandler` devolvía mensajes en formatos y idiomas dispares; los mensajes crudos de Supabase (`errorBody`) llegaban al cliente sin traducir.
4. **Contratos de error ad-hoc:** cada punto de error armaba su payload por su cuenta.

## 2. Solución Aplicada

### A. Cuatro Response DTOs nuevos en `application.port.in.dto`
- `CentroDistribucionResponse`, `UsuarioResponse`, `IngresoMercanciaResponse`, `VentaResponse`, todos con fábrica `from(...)`.

### B. Centralización de errores
- **Nuevo `ErrorResponse`** (`rest/dto`): record inmutable con `timestamp`, `status`, `error`, `message`, `path` y `details`.
- **`ApiExceptionHandler`** (`@RestControllerAdvice`): manejo global de `IllegalArgumentException`, `MethodArgumentNotValidException` y errores de Spring, respondiendo siempre en español con `ErrorResponse`.
- **`SupabaseAuthAdapter`:** los errores crudos de Supabase se traducen a mensajes de usuario en español:
  - `429 / over_email_send_rate_limit` → límite de correos de invitación por hora.
  - `422 / email_address_invalid` → formato de correo inválido.
  - Resto → mensaje genérico de indisponibilidad (sin exponer `errorBody`).

### C. Limpieza
- Eliminado `rest/dto/IngresoMercanciaResponse` (duplicado con la capa de aplicación).
- `.gitignore`: excluye artefactos de audio (`*.mp3`, `*.wav`, `audio_taller.*`).

## 3. Archivos Intervenidos

| Estado | Archivo |
|---|---|
| Nuevos | `port/in/dto/CentroDistribucionResponse.java`, `UsuarioResponse.java`, `IngresoMercanciaResponse.java`, `VentaResponse.java` |
| Nuevo | `rest/dto/ErrorResponse.java` |
| Modificado | `rest/ApiExceptionHandler.java`, `rest/CentroDistribucionController.java`, `rest/IngresoMercanciaController.java`, `rest/UsuarioController.java`, `rest/VentaController.java` |
| Modificado | `rest/dto/RegistrarIngresoMercanciaRequest.java`, `out/auth/SupabaseAuthAdapter.java`, `.gitignore` |
| Eliminado | `rest/dto/IngresoMercanciaResponse.java` |

## 4. Beneficios Obtenidos

1. **Un solo formato de error:** todos los 4xx/5xx responden `ErrorResponse` en español, documentable en Swagger.
2. **Sin filtración de infraestructura:** los cuerpos crudos de Supabase dejan de llegar al cliente final.
3. **Consistencia de agregados:** los 6 dominios del sistema exponen ahora DTOs de aplicación.

## 5. Observaciones

- Los 4 endpoints quedaron con `XResponse.from(...)` **invocado desde el controlador**; la regla final de que el mapeo vive en los servicios (controlador solo delega) llega con `fix35` (servicios) y `fix37` (controladores).
- Este fix completó, junto con `fix30` y `fix31`, la cobertura de Response DTOs para **todos** los agregados.
- Los cambios de `.gitignore` (artefactos de audio) viajaron en este commit por contemporaneidad; no guardan relación con el refactor.
- `graph.html` es un sidecar generado por Maven (`copy-graph-html`).

## 6. Criterios de Aceptación

- [x] Ninguno de los 4 agregados expone entidades de dominio en el HTTP
- [x] `ApiExceptionHandler` + `ErrorResponse` como única fuente de respuestas de error
- [x] Sin `errorBody` crudo de Supabase en respuestas al cliente
- [x] Sin clases de DTO duplicadas entre `rest/dto` y `port/in/dto`
