# Fix 45 — Hardening de seguridad (CSP), limpieza de logs, HU-06 y alcance de H2

**Fecha:** 23 de noviembre de 2026  
**Área:** Seguridad, Logging, Catálogo de Productos y Configuración Maven  
**Referencia:** PR #19 (`feature/sprint3-sistema-logs`, merge `9fd5142` / `933caa9`)

---

## 1. Contexto y Problema

Al integrar la rama `feature/sprint3-sistema-logs` sobre `main` (después de los PRs #18 y #21) se detectaron tres conflictos con `ProductoService` y tres observaciones de review que requerían corrección antes del despliegue:

1. **Conflictos con PR #18:** `ProductoService.java`, `ApiExceptionHandlerTest.java` y `ProductoServiceTest.java` chocaban con los cambios de HTTP 404 y el nuevo parámetro `confirmarConStock` de HU-06.
2. **Dominio externo en CSP:** `SecurityConfig.java` incluía `https://gif.fxtwitter.com` en `img-src` (respaldo de `index.html`).
3. **Empaquetado de H2 en runtime:** `pom.xml` había cambiado `com.h2database:h2` a `<scope>runtime</scope>`, incluyéndolo innecesariamente en el JAR de producción.

---

## 2. Solución Implementada

- **Resolución de conflictos conservando HU-06 y 404:**
  - `ProductoService.cambiarEstadoProducto` mantiene `RecursoNoEncontradoException` (`404`) y exige `confirmarConStock=true` (`409 Conflict`) cuando el producto tiene existencias activas.
  - `ApiExceptionHandlerTest` conserva tanto la prueba de `409 Conflict` como la de `404 Not Found`.
  - `ProductoServiceTest` mantiene las 6 pruebas compatibles con `CambiarEstadoProductoCommand(id, activo, confirmarConStock)`.
- **Hardening en `SecurityConfig.java`:**
  - `SessionCreationPolicy.STATELESS` y cabecera `Content-Security-Policy` estricta, retirando `https://gif.fxtwitter.com` de `img-src`.
- **Logging sin PII:**
  - `SupabaseAuthAdapter` dejó de registrar correos, `errorBody` crudos y `authUserId`; `application.properties` configuró rotación Logback (`10MB`, `14` días, `100MB`) y `spring.jpa.show-sql=false`.
- **Alcance de H2:** devuelto a `<scope>test</scope>` en `pom.xml`.

---

## 3. Archivos Principales

| Archivo | Cambio |
|---|---|
| `infrastructure/config/SecurityConfig.java` | `STATELESS`, CSP estricta sin `gif.fxtwitter.com` |
| `application/service/ProductoService.java` | Unifica 404 y confirmación `confirmarConStock` (409) |
| `infrastructure/adapter/out/auth/SupabaseAuthAdapter.java` | Elimina PII de logs |
| `src/main/resources/application.properties` | Configura niveles, patrón y rotación de logs |
| `pom.xml` | Devuelve `h2` a `<scope>test</scope>` |

---

## 4. Verificación

- `./mvnw.cmd test` ejecutado tras el merge: **214** pruebas ejecutadas, **0** fallos, **0** errores, **0** omitidas (`BUILD SUCCESS`).
