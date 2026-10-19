# Fix 27: Desacoplamiento de Dominio Inventario y Creación de InventarioResponse DTO

## 1. Contexto y Diagnóstico del Problema

Se identificó una deuda técnica arquitectónica en el agregado de inventario:

1. **Fuga de Modelo de Lectura en el Dominio (`Inventario.java`):**
   - La entidad de dominio `Inventario` mantenía campos propios de otro agregado (`nombreProducto`, `categoriaProducto`) y una bandera de presentación (`stockBajo`).
   - Contenía el método `enriquecerConProducto(Producto producto)`, mutando el estado de la entidad con información de catálogo ajena a la responsabilidad del inventario (almacenamiento de existencias físicas).
   - Ninguna otra entidad del sistema (`Venta`, `IngresoMercancia`, `Tienda`, `CentroDistribucion`) presentaba este acoplamiento.

2. **Riesgo de `NullPointerException` en `calcularStockBajo()`:**
   - La evaluación anterior dependía de comparaciones que no controlaban adecuadamente escenarios donde `stockMinimo` o `cantidadActual` fueran `null`.

3. **Ausencia de Metadatos OpenAPI en Controladores de Inventario:**
   - El controlador de inventario exponía directamente el modelo de dominio y carecía de agrupaciones y resúmenes descriptivos en Swagger UI.

---

## 2. Solución Aplicada

### A. Creación del DTO de Lectura `InventarioResponse`
En [InventarioResponse.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/port/in/dto/InventarioResponse.java):
- Se implementó como un Java `record` inmutable con anotaciones `@Schema` de OpenAPI/Swagger y ejemplos realistas.
- Contiene los campos del contrato JSON esperado por el frontend:
  - `id`, `tiendaId`, `centroDistribucionId`, `productoId`, `nombreProducto`, `categoriaProducto`, `cantidadActual`, `stockMinimo`, `stockBajo`.
- Método estático de fábrica idiomático `from(Inventario inventario, Producto producto)` que centraliza el ensamblado y mapeo de datos tolerando `producto == null` defensivamente.

### B. Purificación de la Entidad de Dominio `Inventario`
En [Inventario.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/domain/model/Inventario.java):
- Se eliminaron `nombreProducto`, `categoriaProducto`, `stockBajo` y sus getters/setters.
- Se eliminó el método `enriquecerConProducto(Producto)`.
- Se blindó `calcularStockBajo()` contra nulos:
  ```java
  public boolean calcularStockBajo() {
      if (this.stockMinimo == null) {
          return false;
      }
      int actual = this.cantidadActual != null ? this.cantidadActual : 0;
      return actual < this.stockMinimo;
  }
  ```
- Se preservaron las reglas de negocio puras: `agregarStock()`, `descontarStock()`, `tieneStockDisponible()` y `validarUbicacionExclusiva()`.

### C. Actualización de Puerto de Entrada y Servicio
- En [ConsultarInventarioUseCase.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/port/in/ConsultarInventarioUseCase.java): La firma ahora retorna `List<InventarioResponse>`.
- En [InventarioService.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/application/service/InventarioService.java): Se mapea el stream de entidades con `InventarioResponse.from(i, productosMap.get(i.getProductoId()))`.

### D. Decoración Swagger en `InventarioController`
En [InventarioController.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/main/java/com/example/optiway/infrastructure/adapter/in/rest/InventarioController.java):
- Incorporación de `@Tag(name = "Inventario", description = "Endpoints para consulta y gestión de inventario en tiendas y centros de distribución")`.
- Incorporación de `@Operation` descriptivas para los endpoints de consulta en tienda y en CD.

### E. Actualización de Pruebas Unitarias
En [InventarioServiceTest.java](file:///c:/Users/Fixer/Desktop/Git%20Kraken/OptiWay-Backend/src/test/java/com/example/optiway/InventarioServiceTest.java):
- Actualizado el tipo de retorno a `List<InventarioResponse>`.
- Reemplazadas llamadas a getters tradicionales por accesores canónicos de record (`item.nombreProducto()`, `item.stockBajo()`, etc.).

---

## 3. Verificación

- **Suite de Pruebas:** 37 de 37 tests unitarios y de integración pasando (`BUILD SUCCESS`).
- **Contrato REST:** 100% retrocompatible con la interfaz de usuario web (`inventario.html`).
- **Grafo de Conocimiento:** Sincronizado mediante `graphify update .`.
