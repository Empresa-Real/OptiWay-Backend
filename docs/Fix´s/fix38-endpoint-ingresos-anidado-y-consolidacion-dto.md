# Fix 38 — Consolidación del Ingreso de Mercancía: Endpoint Anidado con Id en URL, Alias CD y Unificación del Request DTO

**Fecha:** 20 de Octubre de 2026  
**Área:** Capa de Infraestructura (Controladores, DTOs REST, Frontend estático) y Documentación  
**Autor:** OptiWay Agent  

---

## 1. Contexto y Diagnóstico

El registro de ingreso de mercancía presentaba cuatro problemas acumulados:

### A. Código Muerto en el Paquete DTO
`RegistrarIngresoRequest.java` (56 líneas) no tenía **ningún uso** en el proyecto: ni controladores, ni servicios, ni pruebas. Era el residuo de una versión anterior del endpoint y confundía a quien buscara "el request del ingreso".

### B. Dos Estilos de Conversión en el Mismo Flujo
`IngresoMercanciaController` construía el `RegistrarIngresoCommand` **a mano** con los getters del DTO, en lugar de delegar la conversión:

```java
RegistrarIngresoCommand command = new RegistrarIngresoCommand(
        request.getCentroDistribucionId(), request.getProductoId(),
        request.getCantidad(), request.getOrigen());
```

Además, `RegistrarIngresoMercanciaRequest` era la **única clase JavaBean** rezagada entre los DTOs de petición (el resto ya eran `record` con `toCommand()` desde Fix 36).

### C. Doble Fuente de Verdad del Identificador del CD
El mismo DTO servía a dos rutas con orígenes distintos para el centro de distribución:
- `POST /api/ingresos-mercancia`: el `centroDistribucionId` venía en el **body**.
- `POST /api/centros-distribucion/{id}/ingresos`: el id venía en la **URL**, pero el campo `@NotNull` del body seguía obligando al cliente a enviarlo **para luego ignorarlo** (`toCommand(centroDistribucionId)` pisaba el valor del body).

### D. Regresión del Alias Corto `/api/cd` (Fix 37)
Fix 22 había mapeado el controlador con **dos rutas base** — `@RequestMapping({"/api/centros-distribucion", "/api/cd"})` — para exponer toda la superficie del CD también bajo el prefijo corto. Fix 37, al estandarizar los controladores, **eliminó el alias sin documentarlo** en su informe; el endpoint `POST /api/cd/{id}/ingresos` dejó de existir silenciosamente.

---

## 2. Solución Implementada

### A. Endpoint Único y Anidado (Decisión del Usuario)
- **Canonical:** `POST /api/centros-distribucion/{id}/ingresos` — el id del receptor viaja en la URL (sub-recurso RESTful, simétrico con `PUT /{id}/encargado`).
- **Eliminados:** la ruta plana `POST /api/ingresos-mercancia` y el controlador `IngresoMercanciaController` (su única responsabilidad se movió al controlador del CD).
- **Alias restaurado:** `POST /api/cd/{id}/ingresos` vuelve a existir mediante el array de rutas base en `@RequestMapping`.

### B. Request DTO Unificado con Id en la URL
`RegistrarIngresoMercanciaRequest` quedó como `record` canónico **sin** `centroDistribucionId` en el body, siguiendo el patrón de Fix 36 (`toCommand(Long id)` fusiona el id del `@PathVariable` con los datos del body):

```java
public record RegistrarIngresoMercanciaRequest(
        @NotNull Long productoId,
        @NotNull @Positive Integer cantidad,
        @NotBlank String origen) {

    public RegistrarIngresoCommand toCommand(Long centroDistribucionId) {
        return new RegistrarIngresoCommand(centroDistribucionId, productoId, cantidad, origen);
    }
}
```

### C. Controlador con Delegación Pura
El endpoint quedó en `CentroDistribucionController` con `@AuthUserId` primero, id en `@PathVariable` y conversión delegada:

```java
@PostMapping("/{id}/ingresos")
@ResponseStatus(HttpStatus.CREATED)
public IngresoMercanciaResponse registrarIngreso(
        @AuthUserId UUID authUserId,
        @PathVariable("id") Long centroDistribucionId,
        @Valid @RequestBody RegistrarIngresoMercanciaRequest request) {
    return registrarIngresoMercanciaUseCase.registrarIngreso(authUserId, request.toCommand(centroDistribucionId));
}
```

### D. Frontend y Documentación Sincronizados
- `ingreso-mercancia.html`: el `fetch` ahora llama a `/api/centros-distribucion/{id}/ingresos` y el body ya no incluye `centroDistribucionId`.
- Documentación de referencia actualizada a la nueva ruta y contrato: HU-18 (ruta, alias, tabla de campos, ejemplo y flujo), HUS-01 (matriz de permisos), HU-29-guía, Swagger_OpenAPI (catálogo de controladores), dto/README + JSON de ejemplo, Sistema-de-Logs (ejemplo de payload) y pendientes.md.
- No se tocaron CHANGELOG ni informes Fix anteriores (documentación histórica).

---

## 3. Archivos

| Archivo | Cambio |
| :--- | :--- |
| `infrastructure/adapter/in/rest/CentroDistribucionController.java` | Alias `/api/cd` restaurado + endpoint `POST /{id}/ingresos` con delegación pura |
| `infrastructure/adapter/in/rest/IngresoMercanciaController.java` | **Eliminado** |
| `infrastructure/adapter/in/rest/dto/RegistrarIngresoMercanciaRequest.java` | `record` sin `centroDistribucionId`; `toCommand(Long)` |
| `infrastructure/adapter/in/rest/dto/RegistrarIngresoRequest.java` | **Eliminado** (código muerto) |
| `resources/static/web/ingreso-mercancia.html` | `fetch` al endpoint anidado; body sin id del CD |
| `docs/Historias de Usuario/.../HU-18`, `HUS-01`, `HU-29-guía` | Ruta, alias, campos y flujo actualizados |
| `docs/Swagger_OpenAPI.md`, `docs/dto/README.md`, `docs/dto/request/*.json` | Contrato del endpoint actualizado |
| `docs/Sistema-de-Logs.md`, `docs/pendientes.md` | Ejemplo de log y checklist actualizados |\n| `infrastructure/adapter/in/rest/GraphController.java` | `@Hidden` — el grafo desaparece de Swagger UI y `/v3/api-docs` (el endpoint sigue público en `/graph.html`) |

---

## 4. Beneficios

- **Una sola forma de hacer ingresos:** dos rutas → un endpoint (dos URLs por alias, cero duplicidad de lógica).
- **Id único y no ambiguo:** el centro de distribución se define en la URL; el body solo lleva datos de la operación (regla path/body de Fix 36 aplicada al pie de la letra).
- **−216 líneas de superficie:** dos clases eliminadas y el DTO más grande del paquete reducido a un record de 22 líneas.
- **Regresión del alias corregida:** `/api/cd/**` vuelve a funcionar y ahora sí está documentado (antes ni Fix 37 lo mencionó).
- **Docs = realidad:** seis documentos de referencia describen exactamente lo que el código hace.

---

## 5. Observaciones

- El trabajo nació como una limpieza iniciada por otro agente (Antigravity) que había borrado el DTO muerto y unificado el record, pero **eliminando el endpoint anidado** para dejar solo la ruta plana. El usuario revirtió esa decisión: el endpoint debe llevar el id en la URL; esta consolidación es la forma final.
- El alias `/api/cd/{id}/ingresos` **se perdió accidentalmente en Fix 37** al reducir `@RequestMapping` de array a string simple; el informe de Fix 37 no lo menciona. Se restaura aquí y queda documentado en HU-18 y Swagger para que no vuelva a desaparecer silenciosamente.
- **Cómo funciona el alias:** un `@RequestMapping({"/api/centros-distribucion", "/api/cd"})` registra **dos rutas base en el mismo controlador**; cada método se publica bajo ambas. No hay dos controladores ni lógica duplicada: es un segundo alias en la tabla de rutas de Spring (mismo handler, misma respuesta). Springdoc listará ambas URLs.
- La validación `@NotNull` sobre `centroDistribucionId` desaparece del body: la fuente de verdad es el `@PathVariable` (validado por Spring como obligatorio → `400`/`404` naturales si falta).\n- A solicitud del usuario, `GraphController` se marcó con `@Hidden`: la operación del grafo de arquitectura **desaparece de Swagger UI y de `/v3/api-docs`**, sin eliminar el endpoint — `/graph`, `/graph/` y `/graph.html` siguen sirviendo la visualización pública (por eso la fila «Grafo de Arquitectura» de la tabla *URLs de Acceso* se mantiene: es una página, no una operación API).
- Los cambios de este fix y del fix39 se dejaron **sin commitear a petición del usuario**, junto con sus informes, para revisión previa.
- Verificación: `.\mvnw clean test -o` → **BUILD SUCCESS, 38/38 tests, 0 fallos**; grafo actualizado (1542 nodos / 3332 aristas).

---

## 6. Criterios de Aceptación

- [x] Existe una única ruta canónica de ingreso: `POST /api/centros-distribucion/{id}/ingresos`.
- [x] El alias `POST /api/cd/{id}/ingresos` funciona y está documentado.
- [x] `RegistrarIngresoMercanciaRequest` es `record` con `toCommand(Long)` y sin id en el body.
- [x] `RegistrarIngresoRequest` (muerto) e `IngresoMercanciaController` (ruta plana) fueron eliminados.
- [x] El frontend `ingreso-mercancia.html` usa el endpoint anidado.
- [x] Ningún documento vigente referencia `/api/ingresos-mercancia` (solo CHANGELOG y fixes históricos).
- [x] Compilación y 38 tests en verde.
