# Fix 44 — HTTP 404 para producto inexistente en edición y cambio de estado

**Fecha:** 23 de noviembre de 2026  
**Área:** Servicios de aplicación, manejador global de excepciones y pruebas  
**Referencia:** PR #18 (`fix/HTTP-404-producto-inexistente`, commit `b82e967`)

---

## 1. Contexto y Problema

Al intentar editar (`PATCH /api/productos/{id}`) o cambiar el estado (`PATCH /api/productos/{id}/estado`) de un producto con un identificador inexistente, `ProductoService` lanzaba `IllegalArgumentException`. `ApiExceptionHandler` mapeaba esa excepción a **HTTP 400 Bad Request**, incumpliendo la especificación de HU-05 y el estándar de la API (**HTTP 404 Not Found**).

---

## 2. Solución Implementada

- **Servicio (`ProductoService.java`):**
  - En `editarProducto` y `cambiarEstadoProducto`, cuando `productoRepositoryPort.findById(command.id())` no encuentra registro, se reemplazó `IllegalArgumentException` por `RecursoNoEncontradoException`.
  - `ApiExceptionHandler.recursoNoEncontrado` ya traduce `RecursoNoEncontradoException` a `@ResponseStatus(HttpStatus.NOT_FOUND)` con cuerpo `ErrorResponse`.
- **Cobertura de pruebas:**
  - Se creó `ProductoServiceTest` verificando que `editarProducto` y `cambiarEstadoProducto` lancen `RecursoNoEncontradoException` sin invocar `save` ni `update`.
  - Se amplió `ApiExceptionHandlerTest` (`recursoNoEncontrado_debeResponderHttp404`) validando el mensaje y la anotación `@ResponseStatus(HttpStatus.NOT_FOUND)`.

---

## 3. Archivos Modificados / Creados

| Archivo | Cambio |
|---|---|
| `application/service/ProductoService.java` | Sustituye `IllegalArgumentException` por `RecursoNoEncontradoException` en `editarProducto` y `cambiarEstadoProducto` |
| `src/test/java/.../ApiExceptionHandlerTest.java` | Agrega verificación de HTTP 404 para `RecursoNoEncontradoException` |
| `src/test/java/.../ProductoServiceTest.java` | Agrega pruebas unitarias de 404 para edición y cambio de estado |

---

## 4. Verificación

- Pruebas ejecutadas tras la integración en `main` (`./mvnw.cmd test`): todas las pruebas de `ProductoServiceTest` y `ApiExceptionHandlerTest` en verde (`BUILD SUCCESS`).
