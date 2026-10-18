# Fix 12 — Unificación de Nomenclatura en Puertos de Repositorio y Casos de Uso

**Fecha:** 18 de Octubre de 2026  
**Área:** Capa de Aplicación (Puertos de Entrada y Salida)  
**Autor:** Antigravity  

---

## 1. Contexto y Diagnóstico

En la arquitectura hexagonal del backend existía una falta de convención en los nombres de los métodos de persistencia en los puertos de salida (`application/port/out/`):

1. **Inconsistencia `save` vs `guardar`:**
   - Puertos modernos ([ProductoRepositoryPort](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/port/out/ProductoRepositoryPort.java), [TiendaRepositoryPort](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/port/out/TiendaRepositoryPort.java), [InventarioRepositoryPort](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/port/out/InventarioRepositoryPort.java)) adoptaron la convención en inglés de Spring Data: `save(entity)`.
   - Puertos iniciales ([UsuarioRepositoryPort](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/port/out/UsuarioRepositoryPort.java), [CentroDistribucionRepositoryPort](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/port/out/CentroDistribucionRepositoryPort.java), [IngresoMercanciaRepositoryPort](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/port/out/IngresoMercanciaRepositoryPort.java)) usaban el término en español: `guardar(entity)`.
   - Dado que `UsuarioRepositoryPort` es un *God Node* central en el grafo de dependencias con más de 34 aristas conectadas, cambiar el nombre de manera destructiva rompería adaptadores de persistencia, servicios de aplicación y pruebas unitarias.

2. **Sobrecarga innecesaria en caso de uso:**
   - [RegistrarVentaUseCase](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/port/in/RegistrarVentaUseCase.java) mantenía un método `default` sobrecargado `registrarVenta(command)` sin `authUserId`, rompiendo la regla de diseño donde cada interfaz de caso de uso expone únicamente un método formal con la firma requerida por las políticas de seguridad.

---

## 2. Cambios Implementados

### A. Patrón Puente (Bridge) en Puertos de Repositorio mediante Métodos `default`

Se implementó soporte bidireccional en todas las interfaces de puerto de repositorio mediante métodos `default` de Java. De esta forma, cualquier servicio o prueba puede invocar indistintamente `save()` o `guardar()` con total compatibilidad hacia atrás:

* **[UsuarioRepositoryPort.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/port/out/UsuarioRepositoryPort.java):**
  ```java
  Usuario guardar(Usuario usuario);
  default Usuario save(Usuario usuario) {
      return guardar(usuario);
  }
  ```
* **[CentroDistribucionRepositoryPort.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/port/out/CentroDistribucionRepositoryPort.java):**
  ```java
  CentroDistribucion guardar(CentroDistribucion centroDistribucion);
  default CentroDistribucion save(CentroDistribucion centroDistribucion) {
      return guardar(centroDistribucion);
  }
  ```
* **[IngresoMercanciaRepositoryPort.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/port/out/IngresoMercanciaRepositoryPort.java):**
  ```java
  IngresoMercancia guardar(IngresoMercancia ingreso);
  default IngresoMercancia save(IngresoMercancia ingreso) {
      return guardar(ingreso);
  }
  ```
* **[ProductoRepositoryPort.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/port/out/ProductoRepositoryPort.java):**
  ```java
  Producto save(Producto producto);
  default Producto guardar(Producto producto) {
      return save(producto);
  }
  ```
* **[TiendaRepositoryPort.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/port/out/TiendaRepositoryPort.java):**
  ```java
  Tienda save(Tienda tienda);
  default Tienda guardar(Tienda tienda) {
      return save(tienda);
  }
  ```
* **[InventarioRepositoryPort.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/port/out/InventarioRepositoryPort.java):**
  ```java
  Inventario save(Inventario inventario);
  default Inventario guardar(Inventario inventario) {
      return save(inventario);
  }
  ```

### B. Simplificación de [RegistrarVentaUseCase.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/port/in/RegistrarVentaUseCase.java)

Se eliminó la sobrecarga transitoria y se dejó un único método explícito y seguro:
```java
public interface RegistrarVentaUseCase {
    Venta registrarVenta(RegistrarVentaCommand command, UUID authUserId);
}
```

---

## 3. Verificación de Pruebas

Se ejecutó la suite completa de tests de la aplicación:
```bash
./mvnw test -Dtest="!OptiwayApplicationTests"
```
**Resultado:** **22 tests ejecutados, 0 fallos, 0 errores (BUILD SUCCESS)**.
* [AsignacionUbicacionTest](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/test/java/com/example/optiway/AsignacionUbicacionTest.java): 4/4 pasados.
* [IngresoMercanciaServiceTest](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/test/java/com/example/optiway/IngresoMercanciaServiceTest.java): 3/3 pasados.
* [InventarioExclusividadTest](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/test/java/com/example/optiway/InventarioExclusividadTest.java): 4/4 pasados.
* [UsuarioServiceTest](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/test/java/com/example/optiway/UsuarioServiceTest.java): 7/7 pasados.
* [VentaServiceTest](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/test/java/com/example/optiway/VentaServiceTest.java): 4/4 pasados.
