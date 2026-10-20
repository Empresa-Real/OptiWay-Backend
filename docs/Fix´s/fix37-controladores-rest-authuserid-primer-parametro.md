# Fix 37 — Controladores REST: @AuthUserId como primer parámetro y delegación pura

> **Commit asociado:** este fix (commit `refactor(rest): ... (Fix 37)`) de la serie de estandarización hexagonal.

## 1. Contexto y Diagnóstico del Problema

1. **Orden de parámetros inconsistente:** `@AuthUserId UUID authUserId` aparecía al final de los handlers (`crearX(request, authUserId)`, `eliminarUsuario(id, authUserId)`), mientras que la convención establecida en puertos y servicios lo colocaba **primero**.
2. **Controladores con lógica de presentación:** ejecutaban `XResponse.from(...)` (mapeo dominio → DTO) e importaban `com.example.optiway.domain.model.*` — la capa de adaptación conocía el dominio.
3. **Excepciones ad-hoc:** se usaba `ResponseStatusException` en algunos handlers en lugar del `ApiExceptionHandler` centralizado.
4. **Documentación incompleta:** varios endpoints no declaraban `@Operation` ni el requisito de autenticación.

## 2. Solución Aplicada

### A. `@AuthUserId` siempre primero (los 7 controladores)
```java
// Antes
ProductoResponse editarProducto(@PathVariable Long id, @Valid @RequestBody EditarProductoRequest request, @AuthUserId UUID authUserId)
// Después
ProductoResponse editarProducto(@AuthUserId UUID authUserId, @PathVariable Long id, @Valid @RequestBody EditarProductoRequest request)
```
Spring resuelve los parámetros por anotación, no por posición: el reorden es 100 % seguro y sin cambios de comportamiento.

### B. Delegación pura
- Controladores solo construyen el Command (`request.toCommand(...)`) y delegan en el caso de uso.
- Cero imports de `domain.model`, cero `XResponse.from(...)`, cero `ResponseStatusException`.

### C. Swagger
- `@Operation` con resumen, descripción y rol requerido en todos los handlers; "Requiere autenticación" añadido a los endpoints de lectura reforzados (`obtenerUsuarioPorId`, `tieneStockActivo`).

## 3. Archivos Intervenidos

- `rest/ProductoController.java`
- `rest/UsuarioController.java`
- `rest/TiendaController.java`
- `rest/CentroDistribucionController.java`
- `rest/InventarioController.java`
- `rest/IngresoMercanciaController.java`
- `rest/VentaController.java`
- `src/main/resources/static/graph.html` (sidecar del knowledge graph, regenerado por la fase `copy-graph-html` de Maven)

## 4. Beneficios Obtenidos

1. **Lectura uniforme:** todo handler se lee `(authUserId, identificador..., request)`; la firma del controller refleja la firma del caso de uso.
2. **Adaptador sin dominio:** la capa REST solo conoce Commands y Response DTOs — hexagonal pura de entrada.
3. **Testeabilidad:** el caso de uso es invocable desde cualquier adaptador con los mismos contratos.

## 5. Observaciones

- El reorden **replica en la capa web** el criterio ya aplicado en `fix28` (puertos) y `fix35` (servicios): con este fix la regla "authUserId primero" queda vigente en las **3 capas, 19/19 puertos y los 7 controladores, sin excepciones**.
- `InventarioController` ya cumplía el orden desde su creación con `ObtenerInventarioCommand` (`fix29`).
- **`graph.html`:** se incluye en este commit para cerrar la serie con `git status` limpio; es un artefacto generado que se reescribe en cada build (histórico del repo muestra el mismo patrón en los commits `738fb90`–`5984ef8`).
- La verificación final de la serie completa: `mvnw clean test -o` → BUILD SUCCESS, **38/38 pruebas, 0 fallos**; grafo actualizado (`graphify update .`: 1508 nodos, 3245 aristas).

## 6. Criterios de Aceptación

- [x] `@AuthUserId UUID authUserId` es el primer parámetro en **todos** los handlers que lo reciben
- [x] Cero imports de `com.example.optiway.domain.model.*` en controladores
- [x] Cero invocaciones de `XResponse.from(...)` fuera de servicios
- [x] Cero `ResponseStatusException` en la capa REST
- [x] `mvnw clean test` → BUILD SUCCESS, 38/38 pruebas
