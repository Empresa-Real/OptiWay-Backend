# Fix 23: Auditoría y Refactorización de Dominio y Puertos de Salida

## 1. Contexto y Diagnóstico del Problema

Durante la auditoría técnica de la capa de Dominio (`domain/model`) y los Puertos de Salida (`application/port/out`), se identificaron las siguientes oportunidades de mejora y deudas técnicas:

1. **Vulnerabilidad de Stock Negativo en Dominio (`Inventario.java`):**
   - El método `descontarStock(int cantidad)` calculaba `this.cantidadActual = actual - cantidad` sin validar que `actual >= cantidad`. Si se invocaba directamente omitiendo la comprobación previa en servicio, el inventario podía registrar balances negativos en la base de datos.
2. **Inconsistencias de Nomenclatura y Tipado en `UsuarioRepositoryPort`:**
   - Se utilizaba `obtenerPorID(Long id)` con `ID` en mayúsculas a diferencia de todos los demás puertos del sistema (`obtenerPorId`).
   - El método `eliminar(Long id)` retornaba la clase wrapper innecesaria `Void` en lugar del tipo primitivo `void`.
3. **Firma Rígida en `ProductoRepositoryPort`:**
   - `findAllById(Iterable<Long> ids)` utilizaba la interfaz genérica `Iterable` en lugar del contrato idiomático `List<Long>`.
   - El método `update` utilizaba `usuarioAuthId` como nombre de parámetro en lugar del estándar `authUserId`.
4. **Fuga de Infraestructura en Dominio (`IngresoMercancia.java`):**
   - La entidad contenía el campo transitorio `stockTotalActualizado`, el cual no pertenece al modelo de persistencia ni al negocio del histórico, sino a la representación DTO de la respuesta REST.
5. **Calificación Completa de Paquete en `InventarioRepositoryPort`:**
   - Se utilizaba `java.util.Optional` calificado inline en lugar de un `import` limpio.
6. **Puertos de Salida Escasos (`VentaRepositoryPort` e `IngresoMercanciaRepositoryPort`):**
   - Carecían de operaciones habituales de lectura (`findById`, `findAll`, `findByCentroDistribucionId`), limitando la capacidad de consulta futura.
7. **Duplicación de Expresión en `Usuario.java`:**
   - La regla compuesta `puedeVerTodoElCatalogoLogistico() || esEncargadoDe(...)` se repetía entre `puedeConsultarTienda` y `puedeOperarCentroDistribucion`.

---

## 2. Solución Aplicada

### A. Dominio Seguro y Limpio
1. **Defensa en Profundidad en `Inventario`:**
   ```java
   if (actual < cantidad) {
       throw new IllegalStateException("Stock insuficiente para realizar el descuento: disponible " + actual + ", solicitado " + cantidad);
   }
   ```
2. **Eliminación de la Fuga de Dominio:**
   - Se removió `stockTotalActualizado` de `IngresoMercancia.java`.
   - El DTO `IngresoMercanciaResponse` ahora proyecta los atributos nativos de la constancia histórica de ingreso.
3. **Consolidación de Autorización en `Usuario`:**
   - Se encapsuló la política en el método privado `tieneAccesoLogisticoOEncargado(boolean esEncargado)`.

### B. Estandarización de Puertos de Salida
1. **`UsuarioRepositoryPort`:**
   - Firma canónica `Optional<Usuario> obtenerPorId(Long id)` y tipo de retorno `void eliminar(Long id)`. Se mantuvo `default Optional<Usuario> obtenerPorID(Long id)` para retrocompatibilidad absoluta.
   - Implementado en `UsuarioRepositoryAdapter` y actualizado en servicios y suites de prueba.
2. **`ProductoRepositoryPort`:**
   - Actualizado a `List<Producto> findAllById(List<Long> ids)` con sobrecarga por defecto para `Iterable`.
   - Parámetro de actualización estandarizado a `authUserId`.
3. **`InventarioRepositoryPort`:**
   - Importación de `java.util.Optional` y firmas limpias `Optional<Inventario>`.
4. **Completitud de Puertos de Consulta:**
   - `VentaRepositoryPort`: Se incorporaron `findById(Long id)`, `obtenerPorId(Long id)` y `findAll()`.
   - `IngresoMercanciaRepositoryPort`: Se incorporaron `findById(Long id)`, `obtenerPorId(Long id)`, `findByCentroDistribucionId(Long cdId)` y `findAll()`.
   - Implementación en `VentaRepositoryAdapter`, `IngresoMercanciaRepositoryAdapter` e `IngresoMercanciaJpaRepository`.

---

## 3. Matriz de Cambios

| Componente | Capa Hexagonal | Modificación |
| :--- | :--- | :--- |
| `Inventario.java` | Dominio | Validación de stock no negativo en `descontarStock`. |
| `Usuario.java` | Dominio | Deduplicación de política de acceso logístico. |
| `IngresoMercancia.java` | Dominio | Eliminado `stockTotalActualizado`. |
| `IngresoMercanciaResponse.java` | Adaptador REST (DTO) | Eliminada dependencia del campo no persistente. |
| `UsuarioRepositoryPort.java` | Puerto de Salida | Renombrado a `obtenerPorId` y retorno `void` en `eliminar`. |
| `UsuarioRepositoryAdapter.java` | Adaptador de Persistencia | Adaptación a firmas canónicas. |
| `ProductoRepositoryPort.java` | Puerto de Salida | Firma `List<Long>` en `findAllById` y parámetro `authUserId`. |
| `ProductoRepositoryAdapter.java` | Adaptador de Persistencia | Implementación de `List<Long>` y `authUserId`. |
| `InventarioRepositoryPort.java` | Puerto de Salida | Import explícito de `Optional`. |
| `VentaRepositoryPort.java` | Puerto de Salida | Métodos `findById`, `findAll`. |
| `VentaRepositoryAdapter.java` | Adaptador de Persistencia | Implementación de consultas de ventas. |
| `IngresoMercanciaRepositoryPort.java` | Puerto de Salida | Métodos `findById`, `findAll`, `findByCentroDistribucionId`. |
| `IngresoMercanciaRepositoryAdapter.java` | Adaptador de Persistencia | Implementación de métodos de consulta de ingresos. |
| `IngresoMercanciaJpaRepository.java` | Persistencia Spring Data | Consulta derivada `findByCentroDistribucionId`. |
| Tests Unitarios | Pruebas | Actualización de mocks y 2 pruebas nuevas de descuento de stock en `InventarioExclusividadTest`. |

---

## 4. Estado de Validación

* **Compilación y Pruebas:** `./mvnw test '-Dtest=!OptiwayApplicationTests'` $\rightarrow$ **BUILD SUCCESS (36/36 tests pasados)**.
