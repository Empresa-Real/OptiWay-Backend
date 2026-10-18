# Fix 14 — Unificación Canónica de Constructores y Migración de Estado en Tiendas

**Fecha:** 18 de Octubre de 2026  
**Área:** Capa de Dominio, Persistencia JPA, Migraciones SQL y Vistas Web  
**Autor:** OptiWay Agent  

---

## 1. Contexto y Diagnóstico

Durante la auditoría de Clean Code, Arquitectura Hexagonal y persistencia relacional se identificaron dos problemas críticos de diseño:

### A. Persistencia Huérfana de Strings Mágicos en Base de Datos (Supabase)
Aunque la entidad JPA [`TiendaJpaEntity`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/out/persistence/TiendaJpaEntity.java) se había actualizado a un booleano `activo`, la tabla real `tiendas` en Supabase PostgreSQL conservaba la columna legacy `estado varchar` con valores textuales (`'Activa'`, `'Activa'`).
* **Causa:** `spring.jpa.hibernate.ddl-auto=update` protege los datos y nunca altera tipos de columnas ni elimina columnas con datos en PostgreSQL. Por ello, la columna legacy continuaba en la base de datos y la interfaz web [`tiendas.html`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/resources/static/tiendas.html) mostraba `undefined` al acceder a `t.estado`.

### B. Anti-Patrón de Sobrecarga de Constructores (*Telescoping Constructors*)
Múltiples entidades de dominio acumulaban constructores sobrecargados con listas posicionales de parámetros variables:
* [`Usuario`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/domain/model/Usuario.java): Constructor histórico de 5 argumentos (`id, nombre, email, rol, creadoPor`) y constructor con `authUserId`.
* [`Tienda`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/domain/model/Tienda.java): Constructor de 6 argumentos (sin `encargadoId`) y constructor completo de 7 argumentos.
* [`Producto`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/domain/model/Producto.java): Constructor mínimo de 3 argumentos (`id, nombre, categoria`) y constructor completo de 7 argumentos.
* [`CentroDistribucion`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/domain/model/CentroDistribucion.java): Constructor de 6 argumentos y constructor completo de 7 argumentos.
* [`Venta`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/domain/model/Venta.java): Constructor sin ID de 4 argumentos y constructor completo de 5 argumentos.

---

## 2. Cambios Implementados

### A. Migración de Base de Datos en Supabase (PostgreSQL)
Se generó el script [fix_tiendas_estado_a_activo.sql](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/docs/migrations/fix_tiendas_estado_a_activo.sql) ejecutado exitosamente en el Editor SQL de Supabase:
1. Creación de la columna `activo boolean DEFAULT true`.
2. Migración condicional de valores existentes (`'Activa'` / `'activo'` $\to$ `true`, resto $\to$ `false`).
3. Configuración de restricciones `NOT NULL` y `DEFAULT true`.
4. Eliminación definitiva de la columna `estado varchar`.

### B. Sincronización en Vista Web ([tiendas.html](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/resources/static/tiendas.html))
Se actualizó el renderizado de la tabla de tiendas para consumir directamente el booleano `t.activo`:
```javascript
// Antes:
<td>${t.estado}</td> // Evaluaba a undefined

// Ahora:
<td>${t.activo !== false ? 'Activa' : 'Inactiva'}</td>
```

### C. Unificación Canónica de Constructores en el Dominio

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

### D. Actualización en Servicios, Adaptadores y Pruebas Unitarias
* **[VentaService.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/service/VentaService.java):** Actualizado para instanciar `new Venta(null, command.tiendaId(), ...)` mediante el constructor canónico.
* **[UsuarioRepositoryAdapter.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/out/persistence/UsuarioRepositoryAdapter.java):** Mapeo simplificado directo mediante el constructor canónico de `Usuario`.
* **Pruebas Unitarias:** Se refactorizaron [`UsuarioServiceTest.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/test/java/com/example/optiway/UsuarioServiceTest.java), [`VentaServiceTest.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/test/java/com/example/optiway/VentaServiceTest.java), [`IngresoMercanciaServiceTest.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/test/java/com/example/optiway/IngresoMercanciaServiceTest.java) y [`AsignacionUbicacionTest.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/test/java/com/example/optiway/AsignacionUbicacionTest.java) eliminando llamadas redundantes a setters posteriores y usando el constructor canónico.

---

## 3. Matriz de Constructores Canónicos

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

## 4. Verificación y Resultados de Tests

Se ejecutó la suite de pruebas unitarias completa con Maven:

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
