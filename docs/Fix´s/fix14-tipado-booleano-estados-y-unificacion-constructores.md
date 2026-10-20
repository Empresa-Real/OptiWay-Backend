# Fix 14 — Tipado Booleano de Estados, Unificación Canónica de Constructores y Migración en Tiendas

**Fecha:** 18 de Octubre de 2026  
**Área:** Capa de Dominio, Persistencia JPA, Migraciones SQL y Vistas Web  
**Autor:** OptiWay Agent  

---

## 1. Contexto y Diagnóstico

Durante la auditoría de Clean Code, Arquitectura Hexagonal y persistencia relacional se identificaron tres problemas de diseño interconectados en el ciclo de vida de `Producto` y `Tienda`:

### A. Strings Mágicos de Estado en el Dominio
Los estados de ciclo de vida se expresaban como **strings planos y mágicos**, con tres consecuencias:

1. **Inconsistencia de mayúsculas/minúsculas:**
   - `Producto` usaba `private String estado = "activo";` (minúscula).
   - `Tienda` usaba `private String estado = "Activa";` (mayúscula inicial).
   - Un cliente o servicio podría escribir `"ACTIVO"`, `"activa"`, `"Inactiva"`, etc., sin que el compilador lo detecte.

2. **Redundancia de fuentes de verdad:**
   - Ambas entidades mantenían simultáneamente un `String estado` y un `Boolean activo`, con lógica de sincronización manual (`setEstado` modificaba `activo`, `setActivo` modificaba `estado`), propensa a desincronización y difícil de mantener.

3. **Falta de restricción en tiempo de compilación:**
   - Un `String` admite cualquier valor, mientras que un `Boolean` restringe el dominio a dos estados válidos: activo/inactivo.

### B. Persistencia Huérfana de Strings Mágicos en Base de Datos (Supabase)
Aunque la entidad JPA [`TiendaJpaEntity`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/out/persistence/TiendaJpaEntity.java) se había actualizado a un booleano `activo`, la tabla real `tiendas` en Supabase PostgreSQL conservaba la columna legacy `estado varchar` con valores textuales (`'Activa'`, `'Inactiva'`).
* **Causa:** `spring.jpa.hibernate.ddl-auto=update` protege los datos y nunca altera tipos de columnas ni elimina columnas con datos en PostgreSQL. Por ello, la columna legacy continuaba en la base de datos y la interfaz web [`tiendas.html`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/resources/static/tiendas.html) mostraba `undefined` al acceder a `t.estado`.

### C. Anti-Patrón de Sobrecarga de Constructores (*Telescoping Constructors*)
Múltiples entidades de dominio acumulaban constructores sobrecargados con listas posicionales de parámetros variables:
* [`Usuario`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/domain/model/Usuario.java): Constructor histórico de 5 argumentos (`id, nombre, email, rol, creadoPor`) y constructor con `authUserId`.
* [`Tienda`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/domain/model/Tienda.java): Constructor de 6 argumentos (sin `encargadoId`) y constructor completo de 7 argumentos.
* [`Producto`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/domain/model/Producto.java): Constructor mínimo de 3 argumentos (`id, nombre, categoria`) y constructor completo de 7 argumentos.
* [`CentroDistribucion`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/domain/model/CentroDistribucion.java): Constructor de 6 argumentos y constructor completo de 7 argumentos.
* [`Venta`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/domain/model/Venta.java): Constructor sin ID de 4 argumentos y constructor completo de 5 argumentos.

---

## 2. Cambios Implementados

### A. Tipado Fuerte con Booleanos en el Dominio y Persistencia

1. **[`Producto.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/domain/model/Producto.java):**
   - Se eliminó el campo `private String estado`.
   - `private Boolean activo = true` pasa a ser la **única fuente de verdad**.
   - Se eliminaron `getEstado()` y `setEstado(String)`.
   - Se mantienen `isActivo()`, `getActivo()` y `setActivo(Boolean)` como API canónica.

2. **[`Tienda.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/domain/model/Tienda.java):**
   - `private String estado` → `private Boolean activo = true`.
   - Se eliminaron los constructores que recibían `String estado`.
   - Se eliminaron `getEstado()` y `setEstado(String)`.
   - Se exponen `getActivo()` y `setActivo(Boolean)`.

3. **[`ProductoJpaEntity.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/out/persistence/ProductoJpaEntity.java):**
   - Se eliminó la columna `@Column private String estado`.
   - Se conserva únicamente `@Column(nullable = false, columnDefinition = "boolean default true") private Boolean activo = true`.
   - Se eliminaron `getEstado()`/`setEstado(String)` y la lógica de sincronización manual entre ambos campos.

4. **[`TiendaJpaEntity.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/out/persistence/TiendaJpaEntity.java):**
   - Se eliminaron `getEstado()` y `setEstado(String)`.
   - **Fase 1 (compatibilidad):** `getActivo()`/`setActivo(Boolean)` quedaron como métodos *derived* sobre la columna `estado` de la base de datos existente, sin alterar el esquema.
   - **Fase 2 (migración):** ver sección 2.C más abajo; terminó mapeando directamente la nueva columna `activo boolean`.

5. **Servicios y adaptadores:**
   - **[`ProductoService.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/service/ProductoService.java):** `producto.setEstado("activo")` → `producto.setActivo(true)`.
   - **[`TiendaService.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/service/TiendaService.java):** `tienda.setEstado("Activa")` → `tienda.setActivo(true)`.
   - **[`ProductoRepositoryAdapter.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/out/persistence/ProductoRepositoryAdapter.java):** Se eliminaron llamadas a `setEstado(...)` redundantes; `setActivo()` es el único mutador utilizado.

### B. Unificación Canónica de Constructores en el Dominio

Cada entidad de dominio ahora cuenta con **exactamente un constructor canónico completo** y un constructor vacío por defecto:

```
┌─────────────────────────┐
│     Entidad Dominio     │
├─────────────────────────┤
│  + Entidad()            │  <- Frameworks / DTO mapping
│  + Entidad(allArgs...)  │  <- Único constructor canónico
└─────────────────────────┘
```

1. **[`Usuario.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/domain/model/Usuario.java):**
   * Se eliminó el constructor de 5 argumentos.
   * Único constructor canónico:
     ```java
     public Usuario(Long id, UUID authUserId, String nombre, String email, Rol rol, UUID creadoPor)
     ```
2. **[`Tienda.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/domain/model/Tienda.java):**
   * Se eliminó el constructor de 6 argumentos.
   * Único constructor canónico:
     ```java
     public Tienda(Long id, String codigo, String nombre, String direccion, String ciudad, Boolean activo, Long encargadoId)
     ```
3. **[`Producto.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/domain/model/Producto.java):**
   * Se eliminó el constructor de 3 argumentos.
   * Único constructor canónico:
     ```java
     public Producto(Long id, String identificador, String nombre, String categoria, BigDecimal precio, String unidadMedida, Boolean activo)
     ```
4. **[`CentroDistribucion.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/domain/model/CentroDistribucion.java):**
   * Se eliminó el constructor de 6 argumentos.
   * Único constructor canónico:
     ```java
     public CentroDistribucion(Long id, String codigo, String nombre, String direccion, Integer capacidad, Long encargadoId, List<Long> tiendasAbastecidasIds)
     ```
5. **[`Venta.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/domain/model/Venta.java):**
   * Se eliminó el constructor de 4 argumentos.
   * Único constructor canónico:
     ```java
     public Venta(Long id, Long tiendaId, Long productoId, Integer cantidad, LocalDateTime fechaHora)
     ```
6. **[`ProductoJpaEntity.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/out/persistence/ProductoJpaEntity.java):**
   * Se eliminó el constructor no utilizado de 3 argumentos.

### C. Migración de Base de Datos en Supabase (PostgreSQL)

Se generó el script [`fix_tiendas_estado_a_activo.sql`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/docs/migrations/fix_tiendas_estado_a_activo.sql) ejecutado exitosamente en el Editor SQL de Supabase:
1. Creación de la columna `activo boolean DEFAULT true`.
2. Migración condicional de valores existentes (`'Activa'` / `'activo'` $\to$ `true`, resto $\to$ `false`).
3. Configuración de restricciones `NOT NULL` y `DEFAULT true`.
4. Eliminación definitiva de la columna `estado varchar`.

Con esta migración concluye la **Fase 2** de `TiendaJpaEntity`: la entidad pasa a mapear directamente `activo boolean` y finaliza el soporte a la columna legacy.

### D. Sincronización en Vista Web ([tiendas.html](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/resources/static/tiendas.html))

Se actualizó el renderizado de la tabla de tiendas para consumir directamente el booleano `t.activo`:
```javascript
// Antes:
<td>${t.estado}</td> // Evaluaba a undefined

// Ahora:
<td>${t.activo !== false ? 'Activa' : 'Inactiva'}</td>
```

### E. Actualización en Servicios, Adaptadores y Pruebas Unitarias

* **[`VentaService.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/service/VentaService.java):** Actualizado para instanciar `new Venta(null, command.tiendaId(), ...)` mediante el constructor canónico.
* **[`UsuarioRepositoryAdapter.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/out/persistence/UsuarioRepositoryAdapter.java):** Mapeo simplificado directo mediante el constructor canónico de `Usuario`.
* **Pruebas Unitarias:** Se refactorizaron [`UsuarioServiceTest.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/test/java/com/example/optiway/UsuarioServiceTest.java), [`VentaServiceTest.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/test/java/com/example/optiway/VentaServiceTest.java), [`IngresoMercanciaServiceTest.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/test/java/com/example/optiway/IngresoMercanciaServiceTest.java) y [`AsignacionUbicacionTest.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/test/java/com/example/optiway/AsignacionUbicacionTest.java) eliminando llamadas redundantes a setters posteriores y usando el constructor canónico.

---

## 3. Matriz Comparativa de Estados

| Aspecto | Antes (Strings Mágicos) | Después (Boolean Tipado) |
| :--- | :--- | :--- |
| **Producto.estado** | `String estado = "activo"` | `Boolean activo = true` |
| **Tienda.estado** | `String estado = "Activa"` | `Boolean activo = true` |
| **Valores válidos** | Cualquier string (`"activo"`, `"Activa"`, `"ACTIVO"`, etc.) | `true` / `false` únicamente |
| **Sincronización** | Manual entre `estado` y `activo` | Única fuente de verdad (`activo`) |
| **Seguridad en compilación** | Ninguna (String admite todo) | Total (solo dos valores posibles) |
| **Columna en Supabase** | `estado varchar` (texto) | `activo boolean NOT NULL DEFAULT true` |

## 4. Matriz de Constructores Canónicos

| Entidad | Parámetros del Constructor Único Canónico |
| :--- | :--- |
| **`Usuario`** | `(Long id, UUID authUserId, String nombre, String email, Rol rol, UUID creadoPor)` |
| **`Tienda`** | `(Long id, String codigo, String nombre, String direccion, String ciudad, Boolean activo, Long encargadoId)` |
| **`Producto`** | `(Long id, String identificador, String nombre, String categoria, BigDecimal precio, String unidadMedida, Boolean activo)` |
| **`CentroDistribucion`** | `(Long id, String codigo, String nombre, String direccion, Integer capacidad, Long encargadoId, List<Long> tiendasAbastecidasIds)` |
| **`Inventario`** | `(Long id, Long tiendaId, Long centroDistribucionId, Long productoId, Integer cantidadActual, Integer stockMinimo)` |
| **`Venta`** | `(Long id, Long tiendaId, Long productoId, Integer cantidad, LocalDateTime fechaHora)` |
| **`IngresoMercancia`** | `(Long id, Long centroDistribucionId, Long productoId, Integer cantidad, String origen, Long usuarioId, LocalDateTime fechaIngreso)` |

---

## 5. Verificación y Resultados

**Compilación:**
```bash
./mvnw compile
```
**Resultado:** `BUILD SUCCESS` — 97 archivos fuente compilados sin errores. No quedan referencias a `getEstado()` ni `setEstado()` en ningún archivo `.java` del proyecto.

**Suite de pruebas unitarias:**
```bash
./mvnw test -Dtest="!OptiwayApplicationTests"
```

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.example.optiway.application.service.AsignacionUbicacionTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.example.optiway.IngresoMercanciaServiceTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.example.optiway.InventarioExclusividadTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.example.optiway.UsuarioServiceTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.example.optiway.VentaServiceTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 22, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## 6. Notas de Retrocompatibilidad

- **Fase 1:** `TiendaJpaEntity.getActivo()` / `setActivo(Boolean)` mapeaban al campo `estado` de la base de datos usando `"Activa"` / `"Inactiva"`. Los registros existentes con `estado = "Activa"` siguieron funcionando correctamente mientras la columna legacy existió.
- **Fase 2:** con la columna `estado` eliminada y migrada a `activo boolean`, la entidad mapea directamente el booleano; la compatibilidad con texto quedó sin efecto.
- `Producto.getEstado()` fue eliminado; cualquier código externo que dependa de este método debe migrarse a `isActivo()` / `getActivo()`.

---

## 7. Observaciones

- Este informe **consolida en un único documento los dos informes que existían con número Fix 14**: `fix14-estados-boolean-tipado-dominio.md` (mitad de código: tipado booleano en dominio, JPA y servicios) y `fix14-unificacion-constructores-y-migracion-estado-tiendas.md` (mitad de infraestructura: migración SQL en Supabase, vista web y unificación de constructores). Ambas mitades eran complementarias del mismo fix y se fusionaron eliminando toda sección duplicada.
- La consolidación resuelve además una **contraducción aparente** entre los dos originales: `TiendaJpaEntity` aparecía mapeando la columna legacy `estado` en uno y ya migrada a `activo` en el otro. En realidad eran dos fases secuenciales del mismo trabajo, ahora narradas en orden (secciones 2.A → 2.C y 6).
- La numeración duplicada provenía de dos sesiones de trabajo paralelas del 18 de Octubre de 2026; a partir de este documento existe un único `fix14`.
- La suite de verificación correspondía a 22 pruebas en esa fecha; hoy la suite es de 38 tests (ver fix28–fix37), todos en verde.
