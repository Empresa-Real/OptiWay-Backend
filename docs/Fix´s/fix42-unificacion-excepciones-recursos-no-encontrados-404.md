# Fix 42 — Unificación de errores para recursos inexistentes (404)

**Fecha:** 22 de noviembre de 2026
**Área:** Servicios de aplicación, controladores REST, pruebas y documentación

---

## 1. Contexto

El punto 13 de `docs/deudaTecnica.md` identificaba respuestas HTTP 400 para búsquedas por identificador de recursos inexistentes. La API ya contaba con `RecursoNoEncontradoException`, manejada por `ApiExceptionHandler` como HTTP 404.

## 2. Solución implementada

- `InventarioService`: las búsquedas de tienda y centro de distribución inexistentes lanzan `RecursoNoEncontradoException`.
- `ProductoService`: edición y cambio de estado de un producto inexistente lanzan `RecursoNoEncontradoException`.
- `UsuarioService`: eliminación y actualización de rol de un usuario inexistente lanzan `RecursoNoEncontradoException`.
- Se actualizaron las pruebas unitarias para verificar el tipo de excepción correspondiente.
- Se agregó una prueba para el cambio de rol cuando el usuario no existe.
- Se documentó el resultado HTTP 404 en Swagger para los seis endpoints afectados.

## 3. Endpoints documentados

| Controlador | Operación | Respuesta de éxito | Error por recurso inexistente |
|---|---|---:|---:|
| `InventarioController` | `GET /api/inventario` | 200 | 404 |
| `InventarioController` | `GET /api/inventario/centro-distribucion` | 200 | 404 |
| `ProductoController` | `PATCH /api/productos/{id}` | 200 | 404 |
| `ProductoController` | `PATCH /api/productos/{id}/estado` | 200 | 404 |
| `UsuarioController` | `DELETE /api/usuarios/{id}` | 204 | 404 |
| `UsuarioController` | `PATCH /api/usuarios/{id}/rol` | 200 | 404 |

## 4. Alcance de las excepciones

Se mantienen las excepciones de validación y reglas de negocio. Por ejemplo, la autoeliminación no permitida continúa siendo `IllegalArgumentException`, y las restricciones de autorización continúan usando `AccesoDenegadoException`.

## 5. Verificación

Se ejecutó la suite completa mediante `./mvnw test`.

**Resultado:** `BUILD SUCCESS`.

- Pruebas ejecutadas: 173.
- Fallos: 0.
- Errores: 0.
- Pruebas omitidas: 0.

También se actualizaron las pruebas de `UsuarioServiceAuthTest` para esperar `RecursoNoEncontradoException` cuando se intenta eliminar un usuario inexistente.

**Estado:** Fix 42 implementado, documentado y verificado.
