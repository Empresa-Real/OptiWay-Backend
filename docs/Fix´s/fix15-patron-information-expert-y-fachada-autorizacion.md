# Fix 15 — Patrón Information Expert y Fachada de Autorización Contextual (ABAC)

**Fecha:** 18 de Octubre de 2026  
**Área:** Dominio, Seguridad de Infraestructura y Servicios de Aplicación  
**Autor:** OptiWay Agent  

---

## 1. Contexto y Diagnóstico

Durante la revisión arquitectónica se detectó el Code Smell de **Feature Envy (Envidia de Funcionalidades)** y dispersión de lógica de autorización en tres servicios de aplicación:

1. **[`VentaService`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/service/VentaService.java):** Extraía el rol del usuario, su ID y el `encargadoId` de la tienda para evaluar si tenía permiso de registrar ventas (`!esAdmin && !esEncargado`).
2. **[`TiendaService`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/service/TiendaService.java):** Inspeccionaba roles para filtrar en memoria con `.stream().filter()` en lugar de consultar la base de datos indexada.
3. **[`CentroDistribucionService`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/service/CentroDistribucionService.java):** Inspeccionaba y comparaba IDs manualmente para restringir la visibilidad de los encargados de CD.

Estos casos no podían resolverse con un simple `requireAdministrador()` porque implementan **reglas diferenciadas de negocio**: Administradores y Planificadores ven toda la red logística, mientras que los Encargados operan únicamente sobre sus instalaciones asignadas (ABAC - Attribute-Based Access Control).

---

## 2. Patrones de Diseño Aplicados (Refactoring Guru / GRASP)

1. **Information Expert (GRASP):** Las preguntas de permisos se mueven a [`Usuario.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/domain/model/Usuario.java), la entidad que posee los atributos (`rol`, `id`).
2. **Facade / Guard (GoF):** [`AuthorizationService.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/security/AuthorizationService.java) actúa como fachada simplificada para consultar identidades y lanzar `AccesoDenegadoException` con mensajes uniformes.
3. **Tell, Don't Ask:** Los servicios no inspeccionan datos ajenos; le piden directamente al usuario: `usuario.puedeOperarTienda(tienda)`.
4. **Filtro Indexado en BD (Pushdown Predicate):** En lugar de traer toda la tabla a memoria con `.filter()`, se delega la consulta a PostgreSQL mediante `tiendaRepositoryPort.obtenerPorEncargadoId(usuario.getId())`.

---

## 3. Cambios Implementados

### A. [`Usuario.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/domain/model/Usuario.java)
Se incorporaron métodos de negocio expresivos:
* `puedeVerTodoElCatalogoLogistico()`: Retorna `true` si es `ADMINISTRADOR` o `PLANIFICADOR`.
* `esEncargadoDe(Tienda tienda)`: Valida rol `ENCARGADO_TIENDA` y coincidencia de ID.
* `esEncargadoDe(CentroDistribucion cd)`: Valida rol `ENCARGADO_CD` y coincidencia de ID.
* `puedeOperarTienda(Tienda tienda)`: Retorna `true` si es Administrador o Encargado de dicha tienda.

### B. [`AuthorizationService.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/security/AuthorizationService.java)
Se extendió la fachada de seguridad con métodos de acceso a recursos:
* `requirePermisoTienda(UUID authUserId, Tienda tienda, String accion)`
* `requirePermisoCentroDistribucion(UUID authUserId, CentroDistribucion cd, String accion)`

### C. [`VentaService.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/service/VentaService.java)
Se eliminó la dependencia innecesaria de `UsuarioRepositoryPort` en favor de la fachada `AuthorizationService`, reduciendo el bloque de validación a una sola línea:
```java
// Reducción de 10 líneas a 1 sola línea limpia:
if (authUserId != null) {
    authorizationService.requirePermisoTienda(authUserId, tienda, "registrar ventas en la tienda especificada.");
}
```

### D. [`TiendaService.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/service/TiendaService.java)
* Reemplazó `usuarioRepositoryPort.obtenerPorAuthUserId(...)` por `authorizationService.requireAuthenticated(authUserId)`.
* Reemplazó `usuario.getRol() == Rol.ADMINISTRADOR || usuario.getRol() == Rol.PLANIFICADOR` por `usuario.puedeVerTodoElCatalogoLogistico()`.
* **Optimización de BD:** Reemplazó `findAll().stream().filter(...)` por consulta SQL directa `tiendaRepositoryPort.obtenerPorEncargadoId(usuario.getId())`.

### E. [`CentroDistribucionService.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/service/CentroDistribucionService.java)
* Reemplazó la búsqueda manual por `authorizationService.requireAuthenticated(authUserId)`.
* Reemplazó comparación manual de roles por `usuario.puedeVerTodoElCatalogoLogistico()`.
* Utiliza referencia a método limpio: `.filter(usuario::esEncargadoDe)`.

### F. Pruebas Unitarias
Se actualizaron [`AsignacionUbicacionTest.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/test/java/com/example/optiway/AsignacionUbicacionTest.java) y [`UsuarioServiceTest.java`](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/test/java/com/example/optiway/UsuarioServiceTest.java) inyectando la instancia de `AuthorizationService(usuarioRepositoryPort)` requerida por los constructores.

---

## 4. Verificación y Resultados de Tests

Ejecución de la suite completa con Maven:

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
