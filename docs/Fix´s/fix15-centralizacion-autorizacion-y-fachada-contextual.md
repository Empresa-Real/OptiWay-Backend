# Fix 15 — Centralización de Autorización (DRY) y Fachada Contextual con Information Expert (ABAC)

**Fecha:** 18 de Octubre de 2026  
**Área:** Capa de Dominio, Servicios de Aplicación, Infraestructura de Seguridad  
**Autor:** OptiWay Agent  

---

## 1. Contexto y Diagnóstico

### A. Violación de DRY: Boilerplate de Autorización Repetido

Durante la auditoría de arquitectura y Clean Code se identificó una violación clara del principio **DRY (Don't Repeat Yourself)** y de la **Responsabilidad Única (SRP)** de SOLID en los servicios de aplicación.

En `ProductoService`, `TiendaService`, `CentroDistribucionService` y `UsuarioService` se repetía el mismo bloque de 6-8 líneas para verificar la identidad y el rol del usuario autenticado:

```java
if (authUserId == null) {
    throw new AccesoDenegadoException("Solo un administrador puede ...");
}
Usuario usuario = usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)
        .orElseThrow(() -> new AccesoDenegadoException("Usuario no encontrado"));
if (usuario.getRol() != Rol.ADMINISTRADOR) {
    throw new AccesoDenegadoException("Solo un administrador puede ...");
}
```

Este patrón aparecía en múltiples métodos de cada servicio (`crearProducto`, `editarProducto`, `cambiarEstadoProducto`, `crearTienda`, `asignarEncargado`, `crearCentroDistribucion`, `asignarEncargadoCD`, `crearUsuario`, `obtenerUsuarios`, `eliminarUsuario`, `actualizarRol`), lo que implica:

- **Duplicación de lógica:** Cualquier cambio en la forma de validar la autorización requiere editar varios servicios.
- **Riesgo de inconsistencia:** Un servicio podría usar un mensaje de error distinto o olvidar validar `authUserId == null`.
- **Acoplamiento:** Cada servicio conoce directamente `UsuarioRepositoryPort`, `Rol` y `AccesoDenegadoException` para fines de autorización, que no son su responsabilidad principal.

### B. Feature Envy: Reglas Contextuales Dispersas (ABAC)

En una segunda pasada se detectó el Code Smell de **Feature Envy (Envidia de Funcionalidades)** en tres servicios que no podían resolverse con un simple `requireAdministrador()` porque implementan **reglas diferenciadas de negocio** (ABAC - Attribute-Based Access Control):

1. **[`VentaService.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/service/VentaService.java):** Extraía el rol del usuario, su ID y el `encargadoId` de la tienda para evaluar si tenía permiso de registrar ventas (`!esAdmin && !esEncargado`).
2. **[`TiendaService.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/service/TiendaService.java):** Inspeccionaba roles para filtrar en memoria con `.stream().filter()` en lugar de consultar la base de datos indexada.
3. **[`CentroDistribucionService.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/service/CentroDistribucionService.java):** Inspeccionaba y comparaba IDs manualmente para restringir la visibilidad de los encargados de CD.

La regla de negocio: Administradores y Planificadores ven toda la red logística, mientras que los Encargados operan únicamente sobre sus instalaciones asignadas.

---

## 2. Patrones de Diseño Aplicados (Refactoring Guru / GRASP)

1. **Information Expert (GRASP):** Las preguntas de permisos se mueven a [`Usuario.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/domain/model/Usuario.java), la entidad que posee los atributos (`rol`, `id`).
2. **Facade / Guard (GoF):** [`AuthorizationService.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/security/AuthorizationService.java) actúa como fachada simplificada para consultar identidades y lanzar `AccesoDenegadoException` con mensajes uniformes.
3. **Tell, Don't Ask:** Los servicios no inspeccionan datos ajenos; le piden directamente al usuario: `usuario.puedeOperarTienda(tienda)`.
4. **Filtro Indexado en BD (Pushdown Predicate):** En lugar de traer toda la tabla a memoria con `.filter()`, se delega la consulta a PostgreSQL mediante `tiendaRepositoryPort.obtenerPorEncargadoId(usuario.getId())`.

---

## 3. Cambios Implementados

### A. [`Usuario.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/domain/model/Usuario.java) — Information Expert

Se enriqueció el modelo de dominio con métodos de consulta de rol y reglas contextuales, encapsulando la lógica de autorización donde pertenece:

```java
// Consulta de rol básica
public boolean esAdministrador() {
    return this.rol == Rol.ADMINISTRADOR;
}

public boolean esEncargado() {
    return this.rol == Rol.ENCARGADO_TIENDA || this.rol == Rol.ENCARGADO_CD;
}

// Reglas contextuales (ABAC)
public boolean puedeVerTodoElCatalogoLogistico();   // ADMINISTRADOR o PLANIFICADOR
public boolean esEncargadoDe(Tienda tienda);        // rol ENCARGADO_TIENDA + coincidencia de ID
public boolean esEncargadoDe(CentroDistribucion cd);// rol ENCARGADO_CD + coincidencia de ID
public boolean puedeOperarTienda(Tienda tienda);    // Administrador o Encargado de dicha tienda
```

### B. [`AuthorizationService.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/security/AuthorizationService.java) (nuevo) — Fachada

Componente `@Component` centralizado que valida identidad y permisos de manera uniforme:

- **`requireAdministrador(authUserId, accion)`**: valida que `authUserId` no sea null, que el usuario exista y que tenga rol `ADMINISTRADOR`. Devuelve el `Usuario` autenticado.
- **`requireAdministradorIfPresent(authUserId, accion)`**: igual que el anterior, pero permite `authUserId == null` (útil para endpoints donde la autenticación es opcional).
- **`requireAuthenticated(authUserId)`**: valida que el usuario exista, sin restricción de rol.
- **`requirePermisoTienda(authUserId, tienda, accion)`**: valida las reglas ABAC de acceso a una tienda concreta.
- **`requirePermisoCentroDistribucion(authUserId, cd, accion)`**: valida las reglas ABAC de acceso a un centro de distribución concreto.

### C. Eliminación de Boilerplate en Servicios de Aplicación (DRY)

Se reemplazó el bloque repetido de 6-8 líneas por una sola llamada a la fachada:

| Servicio | Métodos refactorizados | Antes (líneas por verificación) | Después |
| :--- | :--- | :--- | :--- |
| **ProductoService** | `crearProducto`, `editarProducto`, `cambiarEstadoProducto` | 6-8 líneas | 1 línea |
| **TiendaService** | `crearTienda`, `asignarEncargado` | 6-8 líneas | 1 línea |
| **CentroDistribucionService** | `crearCentroDistribucion`, `asignarEncargado` | 6-8 líneas | 1 línea |
| **UsuarioService** | `crearUsuario`, `obtenerUsuarios`, `eliminarUsuario`, `actualizarRol` | 6-8 líneas + método privado `esAdministrador()` | 1 línea cada uno; método privado eliminado |

**Antes:**
```java
if (authUserId == null) {
    throw new AccesoDenegadoException("Solo un administrador puede crear productos");
}
Usuario usuario = usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)
        .orElseThrow(() -> new AccesoDenegadoException("Usuario no encontrado"));
if (usuario.getRol() != Rol.ADMINISTRADOR) {
    throw new AccesoDenegadoException("Solo un administrador puede crear productos");
}
```

**Después:**
```java
authorizationService.requireAdministrador(authUserId, "crear productos");
```

### D. Reglas Contextuales en Servicios (ABAC)

- **[`VentaService.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/service/VentaService.java):** Se eliminó la dependencia innecesaria de `UsuarioRepositoryPort` en favor de la fachada, reduciendo el bloque de validación de 10 líneas a una sola:
  ```java
  if (authUserId != null) {
      authorizationService.requirePermisoTienda(authUserId, tienda, "registrar ventas en la tienda especificada.");
  }
  ```
- **[`TiendaService.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/service/TiendaService.java):**
  * Reemplazó `usuarioRepositoryPort.obtenerPorAuthUserId(...)` por `authorizationService.requireAuthenticated(authUserId)`.
  * Reemplazó `usuario.getRol() == Rol.ADMINISTRADOR || usuario.getRol() == Rol.PLANIFICADOR` por `usuario.puedeVerTodoElCatalogoLogistico()`.
  * **Optimización de BD:** Reemplazó `findAll().stream().filter(...)` por consulta SQL directa `tiendaRepositoryPort.obtenerPorEncargadoId(usuario.getId())`.
- **[`CentroDistribucionService.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/service/CentroDistribucionService.java):**
  * Reemplazó la búsqueda manual por `authorizationService.requireAuthenticated(authUserId)`.
  * Reemplazó comparación manual de roles por `usuario.puedeVerTodoElCatalogoLogistico()`.
  * Utiliza referencia a método limpio: `.filter(usuario::esEncargadoDe)`.

### E. Pruebas Unitarias

Se actualizaron [`AsignacionUbicacionTest.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/test/java/com/example/optiway/AsignacionUbicacionTest.java) y [`UsuarioServiceTest.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/test/java/com/example/optiway/UsuarioServiceTest.java) inyectando la instancia de `AuthorizationService(usuarioRepositoryPort)` requerida por los constructores.

---

## 4. Matriz Comparativa

| Aspecto | Antes | Después |
| :--- | :--- | :--- |
| **Lógica de autorización (DRY)** | Duplicada en 4 servicios | Centralizada en `AuthorizationService` |
| **Reglas contextuales (ABAC)** | Dispersas en 3 servicios | Fachada `requirePermiso*` + métodos en `Usuario` |
| **Líneas por verificación** | 6-8 líneas (hasta 10 en `VentaService`) | 1 línea |
| **Métodos de dominio en Usuario** | Ninguno | `esAdministrador()`, `esEncargado()`, `puedeVerTodoElCatalogoLogistico()`, `esEncargadoDe()`, `puedeOperarTienda()` |
| **Consistencia de mensajes** | Variable por servicio | Uniforme a través de `AuthorizationService` |
| **Responsabilidad de servicios** | Negocio + autorización | Solo negocio |
| **Listado de tiendas por encargado** | `findAll()` + `filter()` en memoria | SQL indexado `obtenerPorEncargadoId()` |

---

## 5. Verificación y Resultados

**Compilación:**
```bash
./mvnw compile
```
**Resultado:** `BUILD SUCCESS` — 98 archivos fuente compilados sin errores.

**Suite de pruebas unitarias:**
```bash
./mvnw test -Dtest="!OptiwayApplicationTests"
```

```
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

- Los mensajes de error se mantienen semánticamente equivalentes (ej. `"Solo un administrador puede crear productos"`).
- El método privado `esAdministrador(UUID)` de `UsuarioService` fue eliminado; su funcionalidad queda cubierta por `AuthorizationService.requireAdministrador()`.
- `Usuario.esAdministrador()` y `Usuario.esEncargado()` son métodos de consulta aditivos; no modifican el comportamiento existente.
- `AuthorizationService` es un componente nuevo; los servicios que no lo usaban conservaron su comportamiento sin cambios.

---

## 7. Observaciones

- Este informe **consolida en un único documento los dos informes que existían con número Fix 15**: `fix15-centralizacion-autorizacion-roles.md` (mitad DRY: boilerplate de roles en 4 servicios) y `fix15-patron-information-expert-y-fachada-autorizacion.md` (mitad ABAC: Information Expert, fachada contextual y filtro indexado). Ambas mitades describen la evolución del mismo componente, `AuthorizationService`, y se fusionaron sin repetir secciones de `Usuario.java`, `AuthorizationService` ni verificación.
- La numeración duplicada provenía de dos sesiones de trabajo paralelas del 18 de Octubre de 2026; a partir de este documento existe un único `fix15`.
- La pieza DRY (`requireAdministrador` y cía.) fue el primer incremento de `AuthorizationService`; los métodos `requirePermiso*` y las reglas de `Usuario` llegaron en el segundo incremento. Esa progresión queda narrada en orden en las secciones 3.A–3.D.
- `docs/pendientes.md` referencia este trabajo solo por número ("Patrón Information Expert y Fachada de Autorización (Fix 15)"), por lo que la consolidación no rompe ningún enlace.
- La suite de verificación correspondía a 22 pruebas en esa fecha; hoy la suite es de 38 tests (ver fix28–fix37), todos en verde.
