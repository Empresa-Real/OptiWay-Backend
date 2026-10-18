# Fix 16: Optimización N+1 con Batch Querying y Limpieza Integral de Servicios

## 1. Contexto y Diagnóstico del Problema

Durante la revisión de deuda técnica y rendimiento en los servicios de inventario, se identificaron dos debilidades principales:

1. **Problema de Rendimiento N+1 en Inventarios:**
   - En `InventarioService` (Inventario de Tienda): al consultar los productos asociados al inventario de una tienda, el servicio ejecutaba `productoRepositoryPort.findById(inventario.getProductoId())` de manera individual para cada registro dentro del predicado `filter` y nuevamente dentro del transformador `map`. Si una tienda poseía 50 ítems de inventario, se disparaban hasta **100 consultas SQL unitarias** contra la base de datos PostgreSQL.
   - En `ConsultarInventarioCDService` (Inventario de Centros de Distribución): existía un bucle imperativo `for (Long pId : productoIds)` que consultaba uno a uno cada producto mediante `findById`.

2. **Boilerplate y Duplicación Residual de Control de Acceso:**
   - `IngresoMercanciaService`, `ConsultarInventarioCDService` e `InventarioService` mantenían bloques de 15 a 25 líneas con verificaciones manuales de roles (`Rol.ADMINISTRADOR`, `Rol.PLANIFICADOR`, `Rol.ENCARGADO_CD` / `Rol.ENCARGADO_TIENDA`), inyectando innecesariamente `UsuarioRepositoryPort` en lugar de apoyarse en la fachada de seguridad `AuthorizationService` y en el modelo de dominio enriquecido `Usuario`.
   - `ProductoRepositoryAdapter` contenía código repetitivo de mapeo manual en `findById`, `findByNombre` y `findByCategoria` que no reutilizaban el conversor privado `toDomain`.

---

## 2. Solución Aplicada

### A. Soporte para Consultas Batch en el Puerto de Persistencia
Se añadió la firma batch en el puerto de salida `ProductoRepositoryPort`:
```java
List<Producto> findAllById(Iterable<Long> ids);
```

En su adaptador `ProductoRepositoryAdapter`, se implementó delegando directamente al método nativo de Spring Data JPA:
```java
@Override
public List<Producto> findAllById(Iterable<Long> ids) {
    if (ids == null) {
        return List.of();
    }
    return productoJpaRepository.findAllById(ids).stream().map(this::toDomain).toList();
}
```
*Beneficio SQL:* PostgreSQL ahora ejecuta una única sentencia optimizada `SELECT * FROM productos WHERE id IN (?, ?, ...)`, reduciendo la latencia de red y carga sobre la base de datos de $O(N)$ a $O(1)$ viaje de red.

### B. Optimización de Consultas en `InventarioService` y `ConsultarInventarioCDService`
Se sustituyeron los bucles y llamadas unitarias repetitivas por recolección previa de IDs únicos y resolución en memoria vía `Map<Long, Producto>`:
```java
Set<Long> productoIds = inventarios.stream()
        .map(Inventario::getProductoId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());

Map<Long, Producto> productosMap = productoRepositoryPort.findAllById(productoIds).stream()
        .collect(Collectors.toMap(Producto::getId, p -> p));
```
El filtrado y la hidratación de datos del producto (`nombreProducto`, `categoriaProducto`, `stockBajo`) ahora consultan el mapa en memoria en tiempo constante $O(1)$.

### C. Métodos Semánticos en el Dominio (`Usuario`)
Se incorporaron métodos en `Usuario.java` respetando el principio de **Information Expert**:
- `puedeConsultarTienda(Tienda tienda)`: Administrador, Planificador o Encargado asignado.
- `puedeOperarCentroDistribucion(CentroDistribucion cd)`: Administrador, Planificador o Encargado asignado.

### D. Delegación Limpia a `AuthorizationService`
En los servicios de aplicación:
- `InventarioService`:
  ```java
  authorizationService.requirePermisoConsultarTienda(authUserId, tienda, "consultar esta tienda");
  ```
- `ConsultarInventarioCDService`:
  ```java
  authorizationService.requirePermisoCentroDistribucion(authUserId, centro, "consultar el inventario de este centro de distribución");
  ```
- `IngresoMercanciaService`:
  ```java
  Usuario usuario = authorizationService.requirePermisoCentroDistribucion(authUserId, centro, "registrar ingresos en este Centro de Distribución");
  ```

Se eliminaron las inyecciones de `UsuarioRepositoryPort` en estos servicios, reduciendo su acoplamiento.

### E. Limpieza de Boilerplate en `ProductoRepositoryAdapter`
Se refactorizaron `findById`, `findByNombre` y `findByCategoria` para utilizar `this::toDomain`, eliminando más de 40 líneas redundantes de asignación manual de campos.

---

## 3. Resumen de Archivos Modificados

| Archivo | Tipo de Cambio | Impacto |
|---|---|---|
| `ProductoRepositoryPort.java` | Interfaz (Puerto Out) | Agregado `findAllById(Iterable<Long> ids)`. |
| `ProductoRepositoryAdapter.java` | Adaptador JPA | Implementado `findAllById`; simplificados métodos de lectura con `this::toDomain`. |
| `Usuario.java` | Modelo de Dominio | Métodos `puedeConsultarTienda` y `puedeOperarCentroDistribucion`. |
| `AuthorizationService.java` | Fachada de Seguridad | Métodos `requirePermisoCentroDistribucion` y `requirePermisoConsultarTienda`. |
| `InventarioService.java` | Servicio de Aplicación | Eliminado N+1 (doble `findById`); integración con `AuthorizationService`. |
| `ConsultarInventarioCDService.java` | Servicio de Aplicación | Eliminado bucle `for` N+1; integración con `AuthorizationService`. |
| `IngresoMercanciaService.java` | Servicio de Aplicación | Eliminadas 20+ líneas de validación duplicada; uso de `AuthorizationService`. |
| `VentaService.java` | Servicio de Aplicación | Limpieza de imports no utilizados (`Rol`, `Usuario`). |
| `IngresoMercanciaServiceTest.java` | Pruebas Unitarias | Adaptado `setUp` con `AuthorizationService`. |
| `docs/pendientes.md` | Documentación | Marcado el punto 6 (Resolver N+1 en Inventario) como completado. |

---

## 4. Verificación y Resultados de Pruebas

Se ejecutó la suite de pruebas unitarias (`mvn test '-Dtest=!OptiwayApplicationTests'`):
- **Tests ejecutados:** 22
- **Fallos:** 0
- **Errores:** 0
- **Resultado:** `BUILD SUCCESS`
