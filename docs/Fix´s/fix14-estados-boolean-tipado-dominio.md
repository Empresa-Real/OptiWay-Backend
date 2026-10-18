# Fix 14 — Eliminación de Strings Mágicos de Estado y Tipado Fuerte con Booleanos en el Dominio

**Fecha:** 18 de Octubre de 2026  
**Área:** Capa de Dominio, Servicios de Aplicación y Adaptadores de Persistencia  
**Autor:** OptiWay Agent  

---

## 1. Contexto y Diagnóstico

Durante la revisión de arquitectura y Clean Code se identificó que los estados de ciclo de vida de las entidades de dominio se expresaban como **strings planos y mágicos**, lo cual genera los siguientes problemas:

1. **Inconsistencia de mayúsculas/minúsculas:**
   - `Producto` usaba `private String estado = "activo";` (minúscula).
   - `Tienda` usaba `private String estado = "Activa";` (mayúscula inicial).
   - Un cliente o servicio podría escribir `"ACTIVO"`, `"activa"`, `"Inactiva"`, etc., sin que el compilador lo detecte.

2. **Redundancia de fuentes de verdad:**
   - Ambas entidades mantenían simultáneamente un `String estado` y un `Boolean activo`, con lógica de sincronización manual (`setEstado` modificaba `activo`, `setActivo` modificaba `estado`), propensa a desincronización y difícil de mantener.

3. **Falta de restricción en tiempo de compilación:**
   - Un `String` admite cualquier valor, mientras que un `Boolean` restringe el dominio a dos estados válidos: activo/inactivo.

## 2. Cambios Implementados

### A. [Producto.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/domain/model/Producto.java)

- Se eliminó el campo `private String estado`.
- `private Boolean activo = true` pasa a ser la **única fuente de verdad**.
- Se eliminaron `getEstado()` y `setEstado(String)`.
- Se mantienen `isActivo()`, `getActivo()` y `setActivo(Boolean)` como API canónica.

### B. [Tienda.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/domain/model/Tienda.java)

- `private String estado` → `private Boolean activo = true`.
- Se eliminaron los constructores que recibían `String estado`.
- Se eliminaron `getEstado()` y `setEstado(String)`.
- Se exponen `getActivo()` y `setActivo(Boolean)`.

### C. [ProductoJpaEntity.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/out/persistence/ProductoJpaEntity.java)

- Se eliminó la columna `@Column private String estado`.
- Se conserva únicamente `@Column(nullable = false, columnDefinition = "boolean default true") private Boolean activo = true`.
- Se eliminaron `getEstado()`/`setEstado(String)` y la lógica de sincronización manual entre ambos campos.

### D. [TiendaJpaEntity.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/out/persistence/TiendaJpaEntity.java)

- Se eliminaron `getEstado()` y `setEstado(String)`.
- Se mantiene `getActivo()`/`setActivo(Boolean)` como métodos derived del campo `estado` de JPA (para no alterar el esquema de base de datos existente).

### E. Servicios y Adaptadores de Persistencia

- **[ProductoService.java]:** `producto.setEstado("activo")` → `producto.setActivo(true)`.
- **[TiendaService.java]:** `tienda.setEstado("Activa")` → `tienda.setActivo(true)`.
- **[ProductoRepositoryAdapter.java]:** Se eliminaron llamadas a `setEstado(...)` redundantes; `setActivo()` es el único mutador utilizado.

## 3. Matriz Comparativa

| Aspecto | Antes (Strings Mágicos) | Después (Boolean Tipado) |
| :--- | :--- | :--- |
| **Producto.estado** | `String estado = "activo"` | `Boolean activo = true` |
| **Tienda.estado** | `String estado = "Activa"` | `Boolean activo = true` |
| **Valores válidos** | Cualquier string (`"activo"`, `"Activa"`, `"ACTIVO"`, etc.) | `true` / `false` únicamente |
| **Sincronización** | Manual entre `estado` y `activo` | Única fuente de verdad (`activo`) |
| **Seguridad en compilación** | Ninguna (String admite todo) | Total (solo dos valores posibles) |

## 4. Verificación de Compilación

Se ejecutó la compilación del proyecto:

```bash
./mvnw compile
```

**Resultado:** `BUILD SUCCESS` — 97 archivos fuente compilados sin errores.

No quedan referencias a `getEstado()` ni `setEstado()` en ningún archivo `.java` del proyecto.

## 5. Notas de Retrocompatibilidad

- `TiendaJpaEntity.getActivo()` / `setActivo(Boolean)` mapean al campo `estado` de la base de datos usando `"Activa"` / `"Inactiva"`. Los registros existentes con `estado = "Activa"` siguen funcionando correctamente.
- `Producto.getEstado()` fue eliminado; cualquier código externo que dependa de este método debe migrarse a `isActivo()` / `getActivo()`.

---